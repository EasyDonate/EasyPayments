package ru.easydonate.easypayments.service;

import lombok.AllArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.easydonate.easypayments.core.EasyPayments;
import ru.easydonate.easypayments.core.easydonate4j.extension.data.model.EventUpdateReport;
import ru.easydonate.easypayments.core.easydonate4j.extension.data.model.object.CommandReport;
import ru.easydonate.easypayments.core.easydonate4j.extension.data.model.object.NewPaymentReport;
import ru.easydonate.easypayments.database.DatabaseManager;
import ru.easydonate.easypayments.database.model.Customer;
import ru.easydonate.easypayments.database.model.Payment;
import ru.easydonate.easypayments.database.model.Purchase;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@AllArgsConstructor
public final class PersistanceService {

    private static final CompletableFuture<Void> COMPLETED_FUTURE = CompletableFuture.completedFuture(null);

    private final EasyPayments plugin;
    private final DatabaseManager databaseManager;

    public void persistPaymentsReportedState(@NotNull List<EventUpdateReport<?>> reports, @Nullable Predicate<Payment> collectedStateResolver) {
        Map<Integer, Payment> payments = findUnreportedPayments().stream().collect(Collectors.toMap(Payment::getId, p -> p));
        if (payments.isEmpty()) {
            plugin.getDebugLogger().debug("[Persistance] There are no unreported payments in the database");
            return;
        }

        plugin.getDebugLogger().debug("[Persistance] Unreported payments in the database: {0}", payments.keySet());

        CompletableFuture<?>[] futures = reports.stream()
                .map(EventUpdateReport::getReportObjects)
                .flatMap(List::stream)
                .filter(object -> object instanceof NewPaymentReport)
                .map(object -> (NewPaymentReport) object)
                .map(NewPaymentReport::getPaymentId)
                .map(payments::get)
                .filter(Objects::nonNull)
                .filter(Payment::markAsReported)
                .peek(payment -> markAsCollectedIfNeeded(payment, collectedStateResolver))
                .map(databaseManager::savePayment)
                .toArray(CompletableFuture<?>[]::new);

        CompletableFuture.allOf(futures).join();

        plugin.getDebugLogger().debug("[Persistance] Unreported payments have been updated");
    }

    public void populatePurchaseWithReportData(@NotNull Payment payment, int purchaseId, @NotNull List<CommandReport> reports) {
        databaseManager.getPurchase(purchaseId).thenCompose(purchase -> {
            if (purchase != null && purchase.collect(reports)) {
                return databaseManager.savePurchase(purchase).thenCompose(v -> databaseManager.refreshPayment(payment));
            } else {
                return COMPLETED_FUTURE;
            }
        }).join();
    }

    public List<Payment> findUnreportedPayments() {
        return databaseManager.getAllUnreportedPayments(plugin.getServerId()).join();
    }

    public CompletableFuture<?> refreshCustomer(@NotNull Customer customer) {
        return databaseManager.refreshCustomer(customer);
    }

    public CompletableFuture<?> savePayment(@NotNull Payment payment) {
        return databaseManager.savePayment(payment);
    }

    public CompletableFuture<?> savePurchase(@NotNull Purchase purchase) {
        return databaseManager.savePurchase(purchase);
    }

    private void markAsCollectedIfNeeded(Payment payment, Predicate<Payment> collectedStateResolver) {
        if (collectedStateResolver == null || collectedStateResolver.test(payment)) {
            payment.markAsCollected();
        }
    }

}
