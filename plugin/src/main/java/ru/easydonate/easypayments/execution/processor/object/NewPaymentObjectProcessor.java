package ru.easydonate.easypayments.execution.processor.object;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.easydonate.easypayments.core.easydonate4j.PluginEventType;
import ru.easydonate.easypayments.core.easydonate4j.extension.data.model.object.CommandReport;
import ru.easydonate.easypayments.core.easydonate4j.extension.data.model.object.NewPaymentReport;
import ru.easydonate.easypayments.core.easydonate4j.extension.data.model.plugin.PluginEventReport;
import ru.easydonate.easypayments.core.easydonate4j.extension.data.model.plugin.PurchaseNotificationsPluginEventReport;
import ru.easydonate.easypayments.core.easydonate4j.longpoll.data.model.object.NewPaymentEvent;
import ru.easydonate.easypayments.core.easydonate4j.longpoll.data.model.object.PurchasedProduct;
import ru.easydonate.easypayments.core.easydonate4j.longpoll.data.model.plugin.PurchaseNotificationsPluginEvent;
import ru.easydonate.easypayments.core.exception.StructureValidationException;
import ru.easydonate.easypayments.database.DatabaseManager;
import ru.easydonate.easypayments.database.model.Customer;
import ru.easydonate.easypayments.database.model.Payment;
import ru.easydonate.easypayments.database.model.Purchase;
import ru.easydonate.easypayments.execution.ExecutionController;
import ru.easydonate.easypayments.execution.IndexedWrapper;
import ru.easydonate.easypayments.shopcart.ShopCartStorage;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public final class NewPaymentObjectProcessor extends EventObjectProcessor<NewPaymentEvent, NewPaymentReport> {

    private static final CompletableFuture<Void> COMPLETED_FUTURE = CompletableFuture.completedFuture(null);

    private final ExecutionController controller;
    private final ShopCartStorage shopCartStorage;

    public NewPaymentObjectProcessor(@NotNull ExecutionController controller) {
        this.controller = controller;
        this.shopCartStorage = controller.getShopCartStorage();

        super.registerPluginEventProcessor(PluginEventType.PURCHASE_NOTIFICATIONS, this::processPurchaseNotifications);
    }

    private @Nullable PluginEventReport processPurchaseNotifications(@NotNull PurchaseNotificationsPluginEvent pluginEvent) {
        List<String> commands = pluginEvent.getCommands();
        if (commands == null || commands.isEmpty())
            return null;

        List<CommandReport> reports = controller.processCommandsKeepSequence(commands);
        return new PurchaseNotificationsPluginEventReport(reports);
    }

    @Override
    public @NotNull NewPaymentReport processObject(@NotNull NewPaymentEvent eventObject) throws StructureValidationException {
        eventObject.validate();

        int paymentId = eventObject.getPaymentId();
        String customerName = eventObject.getCustomer();
        OfflinePlayer customerPlayer = eventObject.getOfflinePlayer();
        boolean useCart = controller.shopCartConfig().isEnabled();

        NewPaymentReport report = new NewPaymentReport(paymentId, useCart, customerName);
        List<PurchasedProduct> products = eventObject.getProducts();
        products.forEach(PurchasedProduct::validate);

        DatabaseManager databaseManager = controller.getPlugin().getStorage();

        Customer customer = shopCartStorage.getShopCart(customerPlayer, customerName).getCustomer();
        Payment payment = customer.createPayment(paymentId, controller.getServerId());
        databaseManager.savePayment(payment).join();

        // pre-save purchases in the database
        Map<Integer, Integer> productIdsMapping = products.stream()
                .map(payment::createPurchase)
                .parallel()
                .peek(purchase -> databaseManager.savePurchase(purchase).join())
                .collect(Collectors.toMap(Purchase::getProductId, Purchase::getId));

        AtomicInteger indexer = new AtomicInteger();
        if (useCart) {
            // send a notification
            Player onlinePlayer = controller.getPlugin().getServer().getPlayer(customerName);
            if (onlinePlayer != null && onlinePlayer.isOnline()) {
                controller.getMessages().getAndSend(onlinePlayer, "cart-notification");
            }
        } else {
            // execute commands just now
            List<IndexedWrapper<PurchasedProduct>> indexedWrappers = products.stream()
                    .map(product -> new IndexedWrapper<>(indexer.getAndIncrement(), product))
                    .collect(Collectors.toList());

            List<IndexedWrapper<List<CommandReport>>> executionResults = indexedWrappers.parallelStream()
                    .map(product -> executeCommandsAndSavePurchase(payment, product, productIdsMapping))
                    .collect(Collectors.toList());

            executionResults.stream()
                    .sorted(Comparator.comparingInt(IndexedWrapper::getIndex))
                    .map(IndexedWrapper::getObject)
                    .flatMap(List::stream)
                    .forEach(report::addCommandReport);
        }

        controller.refreshCustomer(customer);
        return report;
    }

    private @NotNull IndexedWrapper<List<CommandReport>> executeCommandsAndSavePurchase(
            @NotNull Payment payment,
            @NotNull IndexedWrapper<PurchasedProduct> product,
            @NotNull Map<Integer, Integer> productIdsMapping
    ) {
        AtomicInteger indexer = new AtomicInteger();

        List<String> commands = product.getObject().getCommands();
        List<CommandReport> reports = controller.processCommandsKeepSequence(commands);

        Integer purchaseId = productIdsMapping.get(product.getObject().getId());
        if (purchaseId != null) {
            DatabaseManager databaseManager = controller.getPlugin().getStorage();
            databaseManager.getPurchase(purchaseId).thenCompose(purchase -> {
                if (purchase != null && purchase.collect(reports)) {
                    return databaseManager.savePurchase(purchase).thenRun(() -> controller.refreshPayment(payment));
                } else {
                    return COMPLETED_FUTURE;
                }
            }).join();
        } else {
            controller.getPlugin().getDebugLogger().error("Couldn't find storage purchase ID for product #{0}", product.getObject().getId());
            controller.getPlugin().getDebugLogger().error("Product IDs mapping: {0}", productIdsMapping);
        }

        return new IndexedWrapper<>(product.getIndex(), reports);
    }

}
