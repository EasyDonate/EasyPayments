package ru.easydonate.easypayments.service.processor.object;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.easydonate.easypayments.EasyPaymentsPlugin;
import ru.easydonate.easypayments.core.easydonate4j.PluginEventType;
import ru.easydonate.easypayments.core.easydonate4j.extension.data.model.object.CommandReport;
import ru.easydonate.easypayments.core.easydonate4j.extension.data.model.object.NewPaymentReport;
import ru.easydonate.easypayments.core.easydonate4j.extension.data.model.plugin.PluginEventReport;
import ru.easydonate.easypayments.core.easydonate4j.extension.data.model.plugin.PurchaseNotificationsPluginEventReport;
import ru.easydonate.easypayments.core.easydonate4j.longpoll.data.model.object.NewPaymentEvent;
import ru.easydonate.easypayments.core.easydonate4j.longpoll.data.model.object.PurchasedProduct;
import ru.easydonate.easypayments.core.easydonate4j.longpoll.data.model.plugin.PurchaseNotificationsPluginEvent;
import ru.easydonate.easypayments.core.exception.StructureValidationException;
import ru.easydonate.easypayments.core.util.IndexedWrapper;
import ru.easydonate.easypayments.database.model.Customer;
import ru.easydonate.easypayments.database.model.Payment;
import ru.easydonate.easypayments.database.model.Purchase;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public final class NewPaymentObjectProcessor extends EventObjectProcessor<NewPaymentEvent, NewPaymentReport> {

    private final EasyPaymentsPlugin plugin;

    public NewPaymentObjectProcessor(@NotNull EasyPaymentsPlugin plugin) {
        this.plugin = plugin;
        super.registerPluginEventProcessor(PluginEventType.PURCHASE_NOTIFICATIONS, this::processPurchaseNotifications);
    }

    private @Nullable PluginEventReport processPurchaseNotifications(@NotNull PurchaseNotificationsPluginEvent pluginEvent) {
        List<String> commands = pluginEvent.getCommands();
        if (commands == null || commands.isEmpty())
            return null;

        List<CommandReport> reports = plugin.getExecutionService().processCommandsKeepSequence(commands);
        return new PurchaseNotificationsPluginEventReport(reports);
    }

    @Override
    public @NotNull NewPaymentReport processObject(@NotNull NewPaymentEvent eventObject) throws StructureValidationException {
        eventObject.validate();

        List<PurchasedProduct> products = eventObject.getProducts();
        products.forEach(PurchasedProduct::validate);

        int addToCartCount = (int) products.stream()
                .filter(plugin.getShopCartConfig()::shouldAddToCart)
                .count();

        int paymentId = eventObject.getPaymentId();
        String customerName = eventObject.getCustomer();
        OfflinePlayer customerPlayer = plugin.getPlatformProvider().getOfflinePlayer(customerName);

        Player onlinePlayer = customerPlayer.getPlayer();
        boolean isCustomerOnline = onlinePlayer != null && onlinePlayer.isOnline();
        boolean isAutoIssuanceActive = plugin.getShopCartConfig().shouldIssueWhenOnline() && isCustomerOnline;
        boolean addedToCart = !isAutoIssuanceActive && addToCartCount != 0;
        NewPaymentReport report = new NewPaymentReport(paymentId, addedToCart, customerName);

        Customer customer = plugin.getShopCartStorage().getShopCart(customerPlayer, customerName).getCustomer();
        Payment payment = customer.createPayment(paymentId, plugin.getServerId());
        plugin.getPersistanceService().savePayment(payment).join();

        // pre-save purchases in the database
        List<Purchase> purchases = products.stream().map(payment::createPurchase).collect(Collectors.toList());
        CompletableFuture<?>[] futures = purchases.stream().map(plugin.getPersistanceService()::savePurchase).toArray(CompletableFuture<?>[]::new);
        CompletableFuture.allOf(futures).join();

        if (isAutoIssuanceActive || addToCartCount < products.size()) {
            // execute commands just now
            AtomicInteger indexer = new AtomicInteger();
            List<IndexedWrapper<PurchasedProduct>> indexedWrappers = products.stream()
                    .filter(product -> isAutoIssuanceActive || !plugin.getShopCartConfig().shouldAddToCart(product))
                    .map(product -> new IndexedWrapper<>(indexer.getAndIncrement(), product))
                    .collect(Collectors.toList());

            if (!indexedWrappers.isEmpty()) {
                Map<Integer, Integer> productIdsMapping = purchases.stream().collect(Collectors.toMap(Purchase::getProductId, Purchase::getId));
                List<IndexedWrapper<List<CommandReport>>> commandReports = indexedWrappers.parallelStream()
                        .map(product -> executeCommandAndPersistState(payment, product, productIdsMapping))
                        .collect(Collectors.toList());

                commandReports.stream()
                        .sorted(Comparator.comparingInt(IndexedWrapper::getIndex))
                        .map(IndexedWrapper::getObject)
                        .flatMap(List::stream)
                        .forEach(report::addCommandReport);
            }

            if (!isAutoIssuanceActive && addToCartCount != 0) {
                // send a notification
                if (isCustomerOnline) {
                    plugin.getMessages().getAndSend(onlinePlayer, "cart-notification");
                }
            }
        } else {
            // send a notification
            if (isCustomerOnline) {
                plugin.getMessages().getAndSend(onlinePlayer, "cart-notification");
            }
        }

        plugin.getPersistanceService().refreshCustomer(customer);
        return report;
    }

    private @NotNull IndexedWrapper<List<CommandReport>> executeCommandAndPersistState(
            @NotNull Payment payment,
            @NotNull IndexedWrapper<PurchasedProduct> product,
            @NotNull Map<Integer, Integer> productIdsMapping
    ) {
        AtomicInteger indexer = new AtomicInteger();

        List<String> commands = product.getObject().getCommands();
        List<CommandReport> reports = plugin.getExecutionService().processCommandsKeepSequence(commands);

        Integer purchaseId = productIdsMapping.get(product.getObject().getId());
        if (purchaseId != null) {
            plugin.getPersistanceService().populatePurchaseWithReportData(payment, purchaseId, reports);
        } else {
            plugin.getDebugLogger().error("Couldn't find storage purchase ID for product #{0}", product.getObject().getId());
            plugin.getDebugLogger().error("Product IDs mapping: {0}", productIdsMapping);
        }

        return new IndexedWrapper<>(product.getIndex(), reports);
    }

}
