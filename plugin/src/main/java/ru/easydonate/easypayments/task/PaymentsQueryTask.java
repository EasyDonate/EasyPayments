package ru.easydonate.easypayments.task;

import lombok.AllArgsConstructor;
import lombok.Getter;
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
import ru.easydonate.easypayments.utility.ThrowableToolbox;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class PaymentsQueryTask extends AbstractPluginTask {

    private static final Pattern RESPONSE_CODE_EXCEPTION = Pattern.compile("Server returned HTTP response code: (?<code>\\d+) for URL: (?<url>.+)");

    private static final String THREAD_NAME = "EasyPayments LongPoll Events Listener";
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
            try {
                doQuery();
            } catch (RejectedExecutionException | CancellationException ignored) {
            } catch (Throwable ex) {
                plugin.getLogger().severe("An unexpected error was occurred!");
                ex.printStackTrace();
            }

            if(isWorking()) {
                ThreadLocker.lockUninterruptive(TASK_PERIOD_MILLIS);
            }
        }
    }

    @SneakyThrows(JsonSerializationException.class)
    private void doQuery() {
        if (longPollExecutorService.isShutdown() || longPollExecutorService.isTerminated())
            return;

        try {
            this.longPollQueryTask = CompletableFuture.supplyAsync(this::queryUpdates, longPollExecutorService);

            EventUpdates updates = longPollQueryTask.exceptionally(throwable -> {
                if (throwable instanceof CancellationException || throwable instanceof RejectedExecutionException)
                    return null;

                // bad response time delay
                if (EasyPaymentsPlugin.logQueryTaskErrors() && EasyPaymentsPlugin.isDebugEnabled()) {
                    warning("[Debug] Bad response received from the API Server, just waiting for 60 seconds...");
                }

                ThreadLocker.lockUninterruptive(60L, TimeUnit.SECONDS);
                return null;
            }).join();

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
            if (EasyPaymentsPlugin.logQueryTaskErrors() && EasyPaymentsPlugin.isDebugEnabled()) {
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
            Throwable lastCause = ThrowableToolbox.findLastCause(ex);

            if(lastCause instanceof SocketTimeoutException) {
                // ignore the timeout exception
                return null;

            } else if(lastCause instanceof FileNotFoundException) {
                // handle unknown endpoint response
                String endpointUrl = lastCause.getMessage();
                if(endpointUrl != null && !endpointUrl.isEmpty()) {
                    error("The EasyPayments' endpoint '%s' isn't available now, will try to connect later...", endpointUrl);
                    throw new BadResponseException(404);
                }

            } else if(lastCause instanceof IOException) {
                // handle some HTTP response codes
                try {
                    Matcher matcher = RESPONSE_CODE_EXCEPTION.matcher(lastCause.getMessage());
                    if(matcher.matches()) {
                        int code = Integer.parseInt(matcher.group("code"));
                        String url = matcher.group("url");

                        switch(code) {
                            case 403:
                                // ignore HTTP 403 (access denied)
                                error("Access denied! Please, make sure that you are using a latest version!");
                                break;
                            case 502:
                                // ignore HTTP 502 (ddos protection)
                                error("The EasyPayments LongPoll API endpoint isn't available now, will try to connect later...");
                                error("It may be caused by the work of the DDoS-attack protection of this service :(");
                                break;
                            default:
                                break;
                        }

                        if(code / 100 != 2 && code / 100 != 3)
                            throw new BadResponseException(code);
                    }
                } catch (NumberFormatException ignored) {
                }
            }

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

    @Getter
    @AllArgsConstructor
    private static final class BadResponseException extends RuntimeException {

        private final int httpCode;

    }

}
