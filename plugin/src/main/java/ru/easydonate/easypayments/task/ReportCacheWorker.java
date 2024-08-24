package ru.easydonate.easypayments.task;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.easydonate.easydonate4j.api.v3.exception.ApiResponseFailureException;
import ru.easydonate.easydonate4j.exception.HttpRequestException;
import ru.easydonate.easydonate4j.exception.HttpResponseException;
import ru.easydonate.easypayments.EasyPaymentsPlugin;
import ru.easydonate.easypayments.core.easydonate4j.EventType;
import ru.easydonate.easypayments.core.easydonate4j.extension.data.model.EventUpdateReport;
import ru.easydonate.easypayments.core.easydonate4j.extension.data.model.EventUpdateReports;
import ru.easydonate.easypayments.core.easydonate4j.extension.data.model.object.CommandReport;
import ru.easydonate.easypayments.core.easydonate4j.extension.data.model.object.NewPaymentReport;
import ru.easydonate.easypayments.core.util.ThrowableCauseFinder;
import ru.easydonate.easypayments.database.model.Payment;
import ru.easydonate.easypayments.database.model.Purchase;
import ru.easydonate.easypayments.service.IssuanceReportService;
import ru.easydonate.easypayments.service.PersistanceService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.RejectedExecutionException;

public final class ReportCacheWorker extends AbstractPluginTask {

    private static final long TASK_PERIOD = 6000L;

    private final IssuanceReportService reportService;
    private final PersistanceService persistanceService;

    public ReportCacheWorker(
            @NotNull EasyPaymentsPlugin plugin,
            @NotNull IssuanceReportService reportService,
            @NotNull PersistanceService persistanceService
    ) {
        super(plugin, 20L);
        this.reportService = reportService;
        this.persistanceService = persistanceService;
    }

    @Override
    protected long getPeriod() {
        return TASK_PERIOD;
    }

    @Override
    public void run() {
        if (!isWorking())
            return;

        List<Payment> payments = null;

        // do that synchronously to prevent any conflicts with other tasks
        DATABASE_QUERIES_LOCK.lock();

        try {
            if (isWorking()) {
                payments = persistanceService.findUnreportedPayments();
            }
        } catch (RejectedExecutionException ignored) {
        } finally {
            DATABASE_QUERIES_LOCK.unlock();
        }

        if (payments == null || payments.isEmpty()) {
            updateActivityState();
            return;
        }

        // here the storage instance locking may be required if the storage will be used by any commands
        EventUpdateReports reports = new EventUpdateReports();
        EventUpdateReport<NewPaymentReport> report = new EventUpdateReport<>(EventType.NEW_PAYMENT);
        reports.add(report);

        payments.forEach(payment -> report.addObject(constructReport(payment)));

        try {
            reportService.uploadReports(reports);

            CompletableFuture<?>[] futures = payments.stream()
                    .filter(Payment::markAsReported)
                    .map(persistanceService::savePayment)
                    .toArray(CompletableFuture<?>[]::new);

            CompletableFuture.allOf(futures).join();
        } catch (ApiResponseFailureException ex) {
            // redirect API errors to warning channel
            warning("[ReportCache] Response from API: %s", ex.getMessage());
            plugin.getDebugLogger().warn("[ReportCache] Response from API: {0}", ex.getMessage());
        } catch (HttpRequestException | HttpResponseException ex) {
            Throwable lastCause = ThrowableCauseFinder.findLastCause(ex);

            // ignore HTTP 403 (access denied)
            if (lastCause instanceof IOException && lastCause.getMessage().contains("Server returned HTTP response code: 403")) {
                error("Access denied! Please, make sure that you are using a latest version!");
                plugin.getDebugLogger().error("[ReportCache] Unsupported EasyPayments version (403)");
                updateActivityState();
                return;
            }

            // redirect any other errors to error channel
            error("[ReportCache] %s", ex.getMessage());
            plugin.getDebugLogger().error("[ReportCache] {0}", ex.getMessage());
            plugin.getDebugLogger().error(ex);
        }

        updateActivityState();
    }

    private @NotNull NewPaymentReport constructReport(@NotNull Payment payment) {
        List<CommandReport> commandReports = new ArrayList<>();

        if (payment.hasPurchases())
            payment.getPurchases().stream()
                    .map(this::processPurchase)
                    .filter(Objects::nonNull)
                    .flatMap(List::stream)
                    .forEach(commandReports::add);

        boolean addedToCart = commandReports.isEmpty();
        return new NewPaymentReport(payment.getId(), addedToCart, payment.getCustomer().getPlayerName(), commandReports);
    }

    private @Nullable List<CommandReport> processPurchase(@NotNull Purchase purchase) {
        return purchase.isCollected() ? purchase.constructCommandReports() : null;
    }

}
