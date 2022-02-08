package ru.easydonate.easypayments.execution.processor.object;

import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import ru.easydonate.easypayments.database.DatabaseManager;
import ru.easydonate.easypayments.database.model.Customer;
import ru.easydonate.easypayments.database.model.Payment;
import ru.easydonate.easypayments.database.model.Purchase;
import ru.easydonate.easypayments.easydonate4j.extension.data.model.object.CommandReport;
import ru.easydonate.easypayments.easydonate4j.extension.data.model.object.NewPaymentReport;
import ru.easydonate.easypayments.easydonate4j.longpoll.data.model.object.NewPaymentEvent;
import ru.easydonate.easypayments.easydonate4j.longpoll.data.model.object.PurchasedProduct;
import ru.easydonate.easypayments.exception.StructureValidationException;
import ru.easydonate.easypayments.execution.ExecutionController;
import ru.easydonate.easypayments.execution.IndexedWrapper;
import ru.easydonate.easypayments.shopcart.ShopCartStorage;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public final class NewPaymentObjectProcessor implements EventObjectProcessor<NewPaymentEvent, NewPaymentReport> {

    private final ExecutionController controller;
    private final DatabaseManager databaseManager;
    private final ShopCartStorage shopCartStorage;

    public NewPaymentObjectProcessor(@NotNull ExecutionController controller) {
        this.controller = controller;
        this.databaseManager = controller.getDatabaseManager();
        this.shopCartStorage = controller.getShopCartStorage();
    }

    @Override
    public @NotNull NewPaymentReport processObject(@NotNull NewPaymentEvent eventObject) throws StructureValidationException {
        eventObject.validate();

        int paymentId = eventObject.getPaymentId();
        OfflinePlayer customerPlayer = eventObject.getOfflinePlayer();
        boolean addToCart = controller.shouldAddToCart(customerPlayer);

        NewPaymentReport report = new NewPaymentReport(paymentId, addToCart);
        List<PurchasedProduct> products = eventObject.getProducts();
        products.forEach(PurchasedProduct::validate);

        Customer customer = shopCartStorage.getShopCart(customerPlayer).getCustomer();
        Payment payment = customer.createPayment(paymentId, controller.getServerId());

        AtomicInteger indexer = new AtomicInteger();
        if(addToCart) {
            // add purchases to shop cart
            products.stream()
                    .map(payment::createPurchase)
                    .map(databaseManager::savePurchase)
                    .parallel()
                    .forEach(CompletableFuture::join);
        } else {
            // execute commands just now
            products.stream()
                    .map(product -> new IndexedWrapper<>(indexer.getAndIncrement(), product))
                    .parallel()
                    .map(product -> executeCommandsAndSavePurchase(payment, product))
                    .sequential()
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
            @NotNull IndexedWrapper<PurchasedProduct> product
    ) {
        AtomicInteger indexer = new AtomicInteger();

        List<String> commands = product.getObject().getCommands();
        List<CommandReport> reports = commands.stream()
                .map(command -> controller.processObjectCommandIndexed(command, indexer.getAndIncrement()))
                .parallel()
                .map(CompletableFuture::join)
                .filter(Objects::nonNull)
                .sequential()
                .sorted(Comparator.comparingInt(IndexedWrapper::getIndex))
                .map(IndexedWrapper::getObject)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        Purchase purchase = payment.createPurchase(product.getObject(), reports);
        databaseManager.savePurchase(purchase).join();

        return new IndexedWrapper<>(product.getIndex(), reports);
    }

}
