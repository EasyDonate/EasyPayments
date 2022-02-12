package ru.easydonate.easypayments.task;

import lombok.SneakyThrows;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import ru.easydonate.easydonate4j.api.v3.exception.ApiResponseFailureException;
import ru.easydonate.easydonate4j.exception.HttpRequestException;
import ru.easydonate.easydonate4j.exception.HttpResponseException;
import ru.easydonate.easypayments.EasyPaymentsPlugin;
import ru.easydonate.easypayments.easydonate4j.extension.client.EasyPaymentsClient;
import ru.easydonate.easypayments.easydonate4j.extension.data.model.EventUpdateReports;
import ru.easydonate.easypayments.easydonate4j.longpoll.data.model.EventUpdates;
import ru.easydonate.easypayments.execution.ExecutionController;

import java.util.concurrent.*;

public final class PaymentsQueryTask extends AbstractPluginTask {

    private static final long TASK_PERIOD = 100L;

    private final ExecutionController executionController;
    private final EasyPaymentsClient easyPaymentsClient;

    private final ExecutorService longPollExecutorService;
    private CompletableFuture<EventUpdates> longPollQueryTask;

    public PaymentsQueryTask(
            @NotNull Plugin plugin,
            @NotNull ExecutionController executionController,
            @NotNull EasyPaymentsClient easyPaymentsClient
    ) {
        super(plugin, 100L);

        this.executionController = executionController;
        this.easyPaymentsClient = easyPaymentsClient;

        this.longPollExecutorService = Executors.newSingleThreadExecutor();
    }

    @Override
    protected long getPeriod() {
        return TASK_PERIOD;
    }

    @Override
    public void shutdown() {
        if(bukkitTask == null)
            return;

        this.bukkitTask.cancel();

        try {
            if(longPollQueryTask != null && !longPollQueryTask.isDone())
                longPollQueryTask.cancel(true);

            longPollExecutorService.shutdownNow();
        } catch (Throwable ex) {
            plugin.getLogger().severe("Couldn't correctly shutdown a payments query task!");
            plugin.getLogger().severe(ex.getMessage());
        }

        while(isWorking() && isActive()) {}
        this.bukkitTask = null;
    }

    @SneakyThrows
    @Override
    public void run() {
        if(!isWorking())
            return;

        // do that synchronously to prevent any conflicts with other tasks
        synchronized (executionController.getDatabaseManager()) {
            if(!isWorking())
                return;

            try {
                this.longPollQueryTask = CompletableFuture.supplyAsync(this::queryUpdates, longPollExecutorService);

                EventUpdates updates = longPollQueryTask.join();
                if(updates == null || updates.isEmpty())
                    return;

                System.out.println("LongPoll API updates:");
                System.out.println(updates.toPrettyString());
                EventUpdateReports reports = executionController.processEventUpdates(updates).join();
                executionController.uploadReports(easyPaymentsClient, reports);
            } catch (ApiResponseFailureException ex) {
                // redirect API errors to warning channel
                if(EasyPaymentsPlugin.logQueryTaskErrors() && EasyPaymentsPlugin.isDebugEnabled()) {
                    warning(ex.getMessage());
                }
            } catch (HttpRequestException | HttpResponseException ex) {
                // redirect any other errors to error channel
                if(EasyPaymentsPlugin.logQueryTaskErrors()) {
                    error(ex.getMessage());
                    if(EasyPaymentsPlugin.isDebugEnabled()) {
                        ex.printStackTrace();
                    }
                }
            } catch (RejectedExecutionException | IllegalStateException ignored) {
                // ignore zip file closed and async task termination exceptions
            }
        }

        updateActivityState();
    }

    private @NotNull EventUpdates queryUpdates() {
        try {
            return easyPaymentsClient.getLongPollClient().getUpdatesListSync();
        } catch (ApiResponseFailureException ex) {
            // redirect API errors to warning channel
            if(EasyPaymentsPlugin.logQueryTaskErrors() && EasyPaymentsPlugin.isDebugEnabled()) {
                warning(ex.getMessage());
            }
        } catch (HttpRequestException | HttpResponseException ex) {
            // redirect any other errors to error channel
            if(EasyPaymentsPlugin.logQueryTaskErrors()) {
                error(ex.getMessage());
                if(EasyPaymentsPlugin.isDebugEnabled()) {
                    ex.printStackTrace();
                }
            }
        }

        return null;
    }

}
