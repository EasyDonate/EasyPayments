package ru.easydonate.easypayments.task;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.easydonate.easydonate4j.api.v3.exception.ApiResponseFailureException;
import ru.easydonate.easydonate4j.exception.HttpRequestException;
import ru.easydonate.easydonate4j.exception.HttpResponseException;
import ru.easydonate.easypayments.EasyPaymentsPlugin;
import ru.easydonate.easypayments.database.DatabaseManager;
import ru.easydonate.easypayments.database.model.Payment;
import ru.easydonate.easypayments.database.model.Purchase;
import ru.easydonate.easypayments.core.easydonate4j.EventType;
import ru.easydonate.easypayments.core.easydonate4j.extension.data.model.EventUpdateReport;
import ru.easydonate.easypayments.core.easydonate4j.extension.data.model.EventUpdateReports;
import ru.easydonate.easypayments.core.easydonate4j.extension.data.model.object.CommandReport;
import ru.easydonate.easypayments.core.easydonate4j.extension.data.model.object.NewPaymentReport;
import ru.easydonate.easypayments.execution.ExecutionController;
import ru.easydonate.easypayments.core.util.ThrowableCauseFinder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.RejectedExecutionException;

public final class ReportCacheWorker extends AbstractPluginTask {

    private static final long TASK_PERIOD = 6000L;

    private final ExecutionController executionController;

    public ReportCacheWorker(@NotNull EasyPaymentsPlugin plugin, @NotNull ExecutionController executionController) {
        super(plugin, 20L);
        this.executionController = executionController;
    }

    @Override
    protected long getPeriod() {
        return TASK_PERIOD;
    }

    @Override
    public void run() {
        if (!isWorking())
            return;

        int serverId = executionController.getServerId();
        DatabaseManager databaseManager = executionController.getPlugin().getStorage();

        List<Payment> payments = null;

        // do that synchronously to prevent any conflicts with other tasks
        DATABASE_QUERIES_LOCK.lock();

        try {
            if (isWorking()) {
                payments = databaseManager.getAllUnreportedPayments(serverId).join();
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

        payments.parallelStream()
                .map(this::handlePayment)
                .forEach(report::addObject);

        try {
            executionController.uploadReports(reports);

            payments.stream()
                    .filter(Payment::markAsReported)
                    .map(databaseManager::savePayment)
                    .parallel()
                    .forEach(CompletableFuture::join);
        } catch (ApiResponseFailureException ex) {
            // redirect API errors to warning channel
            if (EasyPaymentsPlugin.logCacheWorkerWarnings() && EasyPaymentsPlugin.isDebugEnabled()) {
                warning(ex.getMessage());
            }
        } catch (HttpRequestException | HttpResponseException ex) {
            Throwable lastCause = ThrowableCauseFinder.findLastCause(ex);

            // ignore HTTP 403 (access denied)
            if (lastCause instanceof IOException && lastCause.getMessage().contains("Server returned HTTP response code: 403")) {
                error("Access denied! Please, make sure that you are using a latest version!");
                updateActivityState();
                return;
            }

            // redirect any other errors to error channel
            if (EasyPaymentsPlugin.logCacheWorkerErrors()) {
                error(ex.getMessage());
                if (EasyPaymentsPlugin.isDebugEnabled()) {
                    ex.printStackTrace();
                }
            }
        }

        updateActivityState();
    }

    private @NotNull NewPaymentReport handlePayment(@NotNull Payment payment) {
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
