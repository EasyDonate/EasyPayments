package ru.easydonate.easypayments.task;

import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import ru.easydonate.easydonate4j.api.v3.exception.ApiResponseFailureException;
import ru.easydonate.easydonate4j.exception.HttpRequestException;
import ru.easydonate.easydonate4j.exception.HttpResponseException;
import ru.easydonate.easydonate4j.exception.JsonSerializationException;
import ru.easydonate.easypayments.EasyPaymentsPlugin;
import ru.easydonate.easypayments.easydonate4j.extension.data.model.EventUpdateReports;
import ru.easydonate.easypayments.easydonate4j.longpoll.data.model.EventUpdates;
import ru.easydonate.easypayments.execution.ExecutionController;
import ru.easydonate.easypayments.utility.ThreadLocker;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;

public final class PaymentsQueryTask extends AbstractPluginTask {

    private static final String THREAD_NAME = "EasyPayments-Query-Task";
    private static final long TASK_PERIOD_MILLIS = 3000L;

    private final ExecutionController executionController;
    private final ExecutorService longPollExecutorService;

    private Thread workingThread;
    private CompletableFuture<EventUpdates> longPollQueryTask;

    public PaymentsQueryTask(@NotNull EasyPaymentsPlugin plugin, @NotNull ExecutionController executionController) {
        super(plugin, 100L);
        this.executionController = executionController;
        this.longPollExecutorService = Executors.newSingleThreadExecutor();
    }

    @Override
    public boolean isWorking() {
        return workingThread != null && !workingThread.isInterrupted() && workingThread.isAlive();
    }

    @Override
    public void start() {
        this.workingThread = new Thread(this, THREAD_NAME);
        this.workingThread.start();
    }

    @Override
    public void shutdown() {
        if(workingThread == null)
            return;

        this.workingThread.interrupt();

        try {
            if(longPollQueryTask != null)
                longPollQueryTask.cancel(true);

            longPollExecutorService.shutdownNow();
        } catch (Throwable ex) {
            plugin.getLogger().severe("Couldn't correctly shutdown a payments query task!");
            plugin.getLogger().severe(ex.getMessage());
        }

        this.workingThread = null;
    }

    @Override
    public void run() {
        ThreadLocker.lockUninterruptive(delay * 50L);

        while(isWorking()) {
            doQuery();

            if(isWorking()) {
                ThreadLocker.lockUninterruptive(TASK_PERIOD_MILLIS);
            }
        }
    }

    @SneakyThrows(JsonSerializationException.class)
    private void doQuery() {
        try {
            this.longPollQueryTask = CompletableFuture.supplyAsync(this::queryUpdates, longPollExecutorService);

            EventUpdates updates = longPollQueryTask.join();
            if(updates == null || updates.isEmpty())
                return;

            if(EasyPaymentsPlugin.isDebugEnabled()) {
                plugin.getLogger().info("[Debug] LongPoll API updates:");
                plugin.getLogger().info(updates.toPrettyString());
            }

            // do that synchronously to prevent any conflicts with other tasks
            DATABASE_QUERIES_LOCK.lock();

            try {
                if(isWorking()) {
                    EventUpdateReports reports = executionController.processEventUpdates(updates).join();
                    executionController.uploadReports(reports);
                }
            } finally {
                DATABASE_QUERIES_LOCK.unlock();
            }
        } catch (ApiResponseFailureException ex) {
            // redirect API errors to warning channel
            if(EasyPaymentsPlugin.logQueryTaskErrors() && EasyPaymentsPlugin.isDebugEnabled()) {
                warning("[Query Task]: %s", ex.getMessage());
            }
        } catch (HttpRequestException | HttpResponseException ex) {
            // redirect any other errors to error channel
            if(EasyPaymentsPlugin.logQueryTaskErrors()) {
                error("[Query Task]: %s", ex.getMessage());
                if(EasyPaymentsPlugin.isDebugEnabled()) {
                    ex.printStackTrace();
                }
            }
        } catch (RejectedExecutionException | IllegalStateException ignored) {
            // ignore zip file closed and async task termination exceptions
        }
    }

    private @NotNull EventUpdates queryUpdates() {
        try {
            return executionController.getEasyPaymentsClient().getLongPollClient().getUpdatesListSync();
        } catch (ApiResponseFailureException ex) {
            // redirect API errors to warning channel
            if(EasyPaymentsPlugin.logQueryTaskErrors() && EasyPaymentsPlugin.isDebugEnabled()) {
                warning("[Query Task]: %s", ex.getMessage());
            }
        } catch (HttpRequestException | HttpResponseException ex) {
            // redirect any other errors to error channel
            if(EasyPaymentsPlugin.logQueryTaskErrors()) {
                error("[Query Task]: %s", ex.getMessage());
                if(EasyPaymentsPlugin.isDebugEnabled()) {
                    ex.printStackTrace();
                }
            }
        }

        return null;
    }

}
