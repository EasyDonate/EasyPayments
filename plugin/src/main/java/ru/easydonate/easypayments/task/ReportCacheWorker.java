package ru.easydonate.easypayments.task;

import org.jetbrains.annotations.NotNull;
import ru.easydonate.easydonate4j.api.v3.exception.ApiResponseFailureException;
import ru.easydonate.easydonate4j.exception.HttpRequestException;
import ru.easydonate.easydonate4j.exception.HttpResponseException;
import ru.easydonate.easypayments.EasyPaymentsPlugin;
import ru.easydonate.easypayments.database.DatabaseManager;
import ru.easydonate.easypayments.database.model.Payment;
import ru.easydonate.easypayments.database.model.Purchase;
import ru.easydonate.easypayments.easydonate4j.EventType;
import ru.easydonate.easypayments.easydonate4j.extension.data.model.EventUpdateReport;
import ru.easydonate.easypayments.easydonate4j.extension.data.model.EventUpdateReports;
import ru.easydonate.easypayments.easydonate4j.extension.data.model.object.CommandReport;
import ru.easydonate.easypayments.easydonate4j.extension.data.model.object.NewPaymentReport;
import ru.easydonate.easypayments.execution.ExecutionController;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

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
        if(!isWorking())
            return;

        int serverId = executionController.getServerId();
        DatabaseManager databaseManager = executionController.getPlugin().getStorage();

        // do that synchronously to prevent any conflicts with other tasks
        List<Payment> payments;
        synchronized (executionController.getPlugin().getStorage()) {
            if(!isWorking())
                return;

            payments = databaseManager.getAllUnreportedPayments(serverId).join();
        }

        if(payments == null || payments.isEmpty()) {
            updateActivityState();
            return;
        }

        // lock to database manager to make stable any working with commands
        synchronized (executionController) {
            if(!isWorking())
                return;

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
                if(EasyPaymentsPlugin.logCacheWorkerWarnings() && EasyPaymentsPlugin.isDebugEnabled()) {
                    warning(ex.getMessage());
                }
            } catch (HttpRequestException | HttpResponseException ex) {
                // redirect any other errors to error channel
                if(EasyPaymentsPlugin.logCacheWorkerErrors()) {
                    error(ex.getMessage());
                    if(EasyPaymentsPlugin.isDebugEnabled()) {
                        ex.printStackTrace();
                    }
                }
            }
        }

        updateActivityState();
    }

    private @NotNull NewPaymentReport handlePayment(@NotNull Payment payment) {
        List<CommandReport> commandReports = new ArrayList<>();

        if(payment.hasPurchases())
            payment.getPurchases().stream()
                    .map(this::processPurchase)
                    .filter(Objects::nonNull)
                    .flatMap(List::stream)
                    .forEach(commandReports::add);

        boolean addedToCart = commandReports.isEmpty();
        return new NewPaymentReport(payment.getId(), addedToCart, commandReports);
    }

    private @NotNull List<CommandReport> processPurchase(@NotNull Purchase purchase) {
        return purchase.isCollected() ? purchase.constructCommandReports() : null;
    }

}
