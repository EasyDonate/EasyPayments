package ru.easydonate.easypayments.task;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.SneakyThrows;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import ru.easydonate.easydonate4j.api.v3.exception.ApiResponseFailureException;
import ru.easydonate.easydonate4j.exception.HttpRequestException;
import ru.easydonate.easydonate4j.exception.HttpResponseException;
import ru.easydonate.easydonate4j.exception.JsonSerializationException;
import ru.easydonate.easypayments.EasyPaymentsPlugin;
import ru.easydonate.easypayments.core.easydonate4j.extension.client.EasyPaymentsClient;
import ru.easydonate.easypayments.core.easydonate4j.extension.data.model.EventUpdateReports;
import ru.easydonate.easypayments.core.easydonate4j.longpoll.data.model.EventUpdates;
import ru.easydonate.easypayments.database.model.Purchase;
import ru.easydonate.easypayments.service.IssuanceReportService;
import ru.easydonate.easypayments.service.LongPollEventDispatcher;
import ru.easydonate.easypayments.core.util.ThreadLocker;
import ru.easydonate.easypayments.core.util.ThrowableCauseFinder;
import ru.easydonate.easypayments.shopcart.ShopCartConfig;

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

    private final EasyPaymentsClient easyPaymentsClient;
    private final LongPollEventDispatcher eventDispatcher;
    private final IssuanceReportService reportService;
    private final ExecutorService longPollExecutor;

    private volatile boolean working;
    private Thread workingThread;
    private CompletableFuture<EventUpdates> longPollQueryTask;

    public PaymentsQueryTask(
            @NotNull EasyPaymentsPlugin plugin,
            @NotNull EasyPaymentsClient easyPaymentsClient,
            @NotNull LongPollEventDispatcher eventDispatcher,
            @NotNull IssuanceReportService reportService
    ) {
        super(plugin, 100L);
        this.easyPaymentsClient = easyPaymentsClient;
        this.eventDispatcher = eventDispatcher;
        this.reportService = reportService;
        this.longPollExecutor = Executors.newSingleThreadExecutor();
    }

    @Override
    public boolean isWorking() {
        return working && workingThread != null && !workingThread.isInterrupted() && workingThread.isAlive();
    }

    @Override
    public void start() {
        this.working = true;
        this.workingThread = new Thread(this, THREAD_NAME);
        this.workingThread.setDaemon(true);
        this.workingThread.start();
    }

    @Override
    public void shutdown() {
        if (workingThread == null)
            return;

        this.working = false;

        try {
            if (longPollQueryTask != null)
                longPollQueryTask.cancel(true);

            longPollExecutor.shutdownNow();
        } catch (Throwable ex) {
            plugin.getLogger().severe("Couldn't correctly shutdown a payments query task!");
            plugin.getLogger().severe(ex.getMessage());
        }

        ThreadLocker.doUninterruptive(() -> workingThread.join());
        this.workingThread = null;
    }

    @Override
    public void run() {
        ThreadLocker.lockUninterruptive(delay * 50L);

        while (isWorking()) {
            try {
                doQuery();
            } catch (RejectedExecutionException | CancellationException ignored) {
            } catch (Throwable ex) {
                plugin.getLogger().severe("An unexpected error was occurred!");
                plugin.getDebugLogger().error("An unexpected error was occurred!");
                plugin.getDebugLogger().error(ex);
            }

            if (isWorking()) {
                ThreadLocker.lockUninterruptive(TASK_PERIOD_MILLIS);
            }
        }
    }

    @SneakyThrows(JsonSerializationException.class)
    private void doQuery() {
        if (longPollExecutor.isShutdown() || longPollExecutor.isTerminated())
            return;

        try {
            this.longPollQueryTask = CompletableFuture.supplyAsync(this::queryUpdates, longPollExecutor);

            EventUpdates updates = longPollQueryTask.exceptionally(throwable -> {
                if (throwable instanceof CancellationException || throwable instanceof RejectedExecutionException)
                    return null;

                if (throwable instanceof CompletionException)
                    throwable = throwable.getCause();

                // bad response time delay
                if (throwable instanceof BadResponseException) {
                    BadResponseException cast = (BadResponseException) throwable;
                    warning(String.format("[PaymentsQuery] Bad response (%d) received from the API Server, waiting for 60 seconds...", cast.getHttpCode()));
                    plugin.getDebugLogger().warn("[PaymentsQuery] Bad response ({0}) received from the API Server, waiting for 60 seconds...", cast.getHttpCode());
                } else {
                    warning("[PaymentsQuery] Bad response received from the API Server, waiting for 60 seconds...");
                    plugin.getDebugLogger().warn("[PaymentsQuery] Bad response received from the API Server, waiting for 60 seconds...");

                    if (throwable != null)
                        plugin.getDebugLogger().warn(throwable);
                }

                ThreadLocker.lockUninterruptive(60L, TimeUnit.SECONDS);
                return null;
            }).join();

            if (updates == null || updates.isEmpty())
                return;

            plugin.getDebugLogger().debug("[PaymentsQuery] LongPoll API updates received:");
            plugin.getDebugLogger().debug(updates.toPrettyString().split("\n"));

            // do that synchronously to prevent any conflicts with other tasks
            DATABASE_QUERIES_LOCK.lock();

            try {
                ShopCartConfig shopCartConfig = plugin.getShopCartConfig();
                EventUpdateReports reports = eventDispatcher.processEventUpdates(updates).join();
                reportService.uploadReportsAndPersistStates(reports, payment -> {
                    if (payment == null || !payment.hasPurchases() || !shopCartConfig.isEnabled())
                        return true;

                    if (shopCartConfig.shouldIssueWhenOnline()) {
                        Player onlinePlayer = plugin.getServer().getPlayer(payment.getCustomer().getPlayerName());
                        if (onlinePlayer != null && onlinePlayer.isOnline()) {
                            return true;
                        }
                    }

                    return payment.getPurchases().stream()
                            .mapToInt(Purchase::getProductId)
                            .noneMatch(shopCartConfig::shouldAddToCart);
                });
            } finally {
                DATABASE_QUERIES_LOCK.unlock();
            }
        } catch (ApiResponseFailureException ex) {
            // redirect API errors to warning channel
            warning("[PaymentsQuery] Response from API: %s", ex.getMessage());
            plugin.getDebugLogger().warn("[PaymentsQuery] Response from API: {0}", ex.getMessage());
        } catch (HttpRequestException | HttpResponseException ex) {
            // redirect any other errors to error channel
            error("[PaymentsQuery] %s", ex.getMessage());
            plugin.getDebugLogger().error("[PaymentsQuery] {0}", ex.getMessage());
            plugin.getDebugLogger().error(ex);
        } catch (RejectedExecutionException | IllegalStateException ignored) {
            // ignore zip file closed and async task termination exceptions
        }
    }

    private @NotNull EventUpdates queryUpdates() {
        try {
            return easyPaymentsClient.getLongPollClient().getUpdatesListSync();
        } catch (ApiResponseFailureException ex) {
            // redirect API errors to warning channel
            warning("[PaymentsQuery] Response from API: %s", ex.getMessage());
            plugin.getDebugLogger().warn("[PaymentsQuery] Response from API: {0}", ex.getMessage());
        } catch (HttpRequestException | HttpResponseException ex) {
            Throwable lastCause = ThrowableCauseFinder.findLastCause(ex);
            if (lastCause instanceof SocketTimeoutException) {
                // ignore the timeout exception
                return null;
            } else if (lastCause instanceof FileNotFoundException) {
                // handle unknown endpoint response
                String endpointUrl = lastCause.getMessage();
                if (endpointUrl != null && !endpointUrl.isEmpty()) {
                    error("[PaymentsQuery] The EasyPayments endpoint '%s' isn't available now, I'll try to connect later...", endpointUrl);
                    plugin.getDebugLogger().error("[PaymentsQuery] The EasyPayments endpoint '{0}' isn't available now", endpointUrl);
                    plugin.getDebugLogger().error(lastCause);
                    throw new BadResponseException(404);
                }
            } else if (lastCause instanceof IOException) {
                // handle some HTTP response codes
                try {
                    Matcher matcher = RESPONSE_CODE_EXCEPTION.matcher(lastCause.getMessage());
                    if (matcher.matches()) {
                        int code = Integer.parseInt(matcher.group("code"));
                        String url = matcher.group("url");

                        switch(code) {
                            case 403:
                                // ignore HTTP 403 (access denied)
                                error("Access denied! Please, make sure that you are using a latest version!");
                                plugin.getDebugLogger().error("[PaymentsQuery] Unsupported EasyPayments version (403)");
                                break;
                            case 502:
                                // ignore HTTP 502 (ddos protection)
                                error("The EasyPayments LongPoll API endpoint isn't available now, will try to connect later...");
                                error("It may be caused by the work of the DDoS-attack protection of this service :(");
                                plugin.getDebugLogger().error("[PaymentsQuery] EasyDonate API is unavailable (502)");
                                break;
                            default:
                                break;
                        }

                        if (code / 100 != 2 && code / 100 != 3)
                            throw new BadResponseException(code);
                    }
                } catch (NumberFormatException ignored) {
                }
            }

            // redirect any other errors to error channel
            error("[PaymentsQuery] %s", ex.getMessage());
            plugin.getDebugLogger().error("[PaymentsQuery] {0}", ex.getMessage());
            plugin.getDebugLogger().error(ex);
        }

        return null;
    }

    @Getter
    @AllArgsConstructor
    private static final class BadResponseException extends RuntimeException {

        private final int httpCode;

    }

}
