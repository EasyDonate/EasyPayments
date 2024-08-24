package ru.easydonate.easypayments.service;

import lombok.AllArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.easydonate.easydonate4j.exception.HttpRequestException;
import ru.easydonate.easydonate4j.exception.HttpResponseException;
import ru.easydonate.easypayments.core.EasyPayments;
import ru.easydonate.easypayments.core.easydonate4j.EventType;
import ru.easydonate.easypayments.core.easydonate4j.extension.data.model.EventUpdateReport;
import ru.easydonate.easypayments.core.easydonate4j.extension.data.model.EventUpdateReports;
import ru.easydonate.easypayments.core.easydonate4j.extension.data.model.object.CommandReport;
import ru.easydonate.easypayments.core.easydonate4j.extension.data.model.object.NewPaymentReport;
import ru.easydonate.easypayments.database.model.Payment;
import ru.easydonate.easypayments.database.model.Purchase;
import ru.easydonate.easypayments.service.execution.ExecutionService;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@AllArgsConstructor
public final class IssuancePerformService {

    private final EasyPayments plugin;
    private final IssuanceReportService reportService;
    private final ExecutionService executionService;
    private final PersistanceService persistanceService;

    public void issuePurchasesAndReport(@NotNull Collection<Payment> payments) throws HttpRequestException, HttpResponseException {
        issuePurchasesAndReport(payments, null);
    }

    public void issuePurchasesAndReport(
            @NotNull Collection<Payment> payments,
            @Nullable Predicate<Purchase> purchaseFilter
    ) throws HttpRequestException, HttpResponseException {
        List<NewPaymentReport> reports = issuePurchases(payments, purchaseFilter);
        if (reports.isEmpty())
            return;

        EventUpdateReport<NewPaymentReport> updateReport = new EventUpdateReport<>(EventType.NEW_PAYMENT, reports);
        EventUpdateReports updateReports = new EventUpdateReports(updateReport);
        reportService.uploadReports(updateReports);
    }

    public @NotNull List<NewPaymentReport> issuePurchases(@NotNull Collection<Payment> payments) {
        return issuePurchases(payments, null);
    }

    public @NotNull List<NewPaymentReport> issuePurchases(
            @NotNull Collection<Payment> payments,
            @Nullable Predicate<Purchase> purchaseFilter
    ) {
        payments = excludeFullyNonMatched(payments, purchaseFilter);
        if (payments.isEmpty())
            return Collections.emptyList();

        plugin.getDebugLogger().debug("[Issuance] Marking payments as collected...");

        CompletableFuture<?>[] futures = payments.stream()
                .filter(payment -> hasAllPurchasesMatched(payment, p -> p.isCollected() || purchaseFilter == null || purchaseFilter.test(p)))
                .filter(Payment::markAsCollected)
                .map(persistanceService::savePayment)
                .toArray(CompletableFuture<?>[]::new);

        CompletableFuture.allOf(futures).join();
        plugin.getDebugLogger().debug("[Issuance] {0} payment(s) have been marked as collected", futures.length);

        plugin.getDebugLogger().debug("[Issuance] Issuing purchases from {0} payment(s)...", payments.size());
        return payments.parallelStream()
                .map(payment -> handlePayment(payment, purchaseFilter))
                .collect(Collectors.toList());
    }

    private @NotNull NewPaymentReport handlePayment(@NotNull Payment payment, @Nullable Predicate<Purchase> purchaseFilter) {
        String customer = payment.getCustomer().getPlayerName();
        NewPaymentReport report = new NewPaymentReport(payment.getId(), false, customer);

        if (payment.hasPurchases()) {
            payment.getPurchases().parallelStream()
                    .filter(purchase -> purchaseFilter == null || purchaseFilter.test(purchase))
                    .filter(Purchase::hasCommands)
                    .map(purchase -> handlePurchase(customer, purchase))
                    .flatMap(List::stream)
                    .forEach(report::addCommandReport);
        }

        return report;
    }

    private @NotNull List<CommandReport> handlePurchase(String customer, @NotNull Purchase purchase) {
        List<String> commands = purchase.getCommands();
        if (commands != null && !commands.isEmpty())
            commands = commands.stream()
                    .map(command -> command != null ? command.replace("{user}", customer) : command)
                    .collect(Collectors.toList());

        List<CommandReport> commandReports = executionService.processCommandsKeepSequence(commands);
        purchase.collect(commandReports);

        persistanceService.savePurchase(purchase).join();
        return commandReports;
    }

    private @NotNull Collection<Payment> excludeFullyNonMatched(@NotNull Collection<Payment> payments, @Nullable Predicate<Purchase> purchaseFilter) {
        if (payments.isEmpty() || purchaseFilter == null)
            return payments;

        return payments.stream()
                .filter(payment -> !hasAllPurchasesNonMatched(payment, purchaseFilter))
                .collect(Collectors.toList());
    }

    private boolean hasAllPurchasesMatched(@NotNull Payment payment, @Nullable Predicate<Purchase> purchaseFilter) {
        if (purchaseFilter == null || !payment.hasPurchases())
            return true;

        return payment.getPurchases().stream().allMatch(purchaseFilter);
    }

    private boolean hasAllPurchasesNonMatched(@NotNull Payment payment, @Nullable Predicate<Purchase> purchaseFilter) {
        if (purchaseFilter == null || !payment.hasPurchases())
            return true;

        return payment.getPurchases().stream().noneMatch(purchaseFilter);
    }

}
