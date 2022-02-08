package ru.easydonate.easypayments.execution.processor.object;

import org.jetbrains.annotations.NotNull;
import ru.easydonate.easypayments.easydonate4j.extension.data.model.object.RepeatPaymentReport;
import ru.easydonate.easypayments.easydonate4j.longpoll.data.model.object.PurchasedProduct;
import ru.easydonate.easypayments.easydonate4j.longpoll.data.model.object.RepeatPaymentEvent;
import ru.easydonate.easypayments.exception.StructureValidationException;
import ru.easydonate.easypayments.execution.ExecutionController;
import ru.easydonate.easypayments.execution.IndexedWrapper;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

public final class RepeatPaymentObjectProcessor implements EventObjectProcessor<RepeatPaymentEvent, RepeatPaymentReport> {

    private final ExecutionController controller;

    public RepeatPaymentObjectProcessor(@NotNull ExecutionController controller) {
        this.controller = controller;
    }

    @Override
    public @NotNull RepeatPaymentReport processObject(@NotNull RepeatPaymentEvent eventObject) throws StructureValidationException {
        eventObject.validate();

        int paymentId = eventObject.getPaymentId();
        RepeatPaymentReport report = new RepeatPaymentReport(paymentId);
        List<PurchasedProduct> products = eventObject.getProducts();
        products.forEach(PurchasedProduct::validate);

        // execute commands just now
        AtomicInteger indexer = new AtomicInteger();
        products.stream()
                .map(PurchasedProduct::getCommands)
                .flatMap(List::stream)
                .map(command -> controller.processObjectCommandIndexed(command, indexer.getAndIncrement()))
                .parallel()
                .map(CompletableFuture::join)
                .filter(Objects::nonNull)
                .sequential()
                .sorted(Comparator.comparingInt(IndexedWrapper::getIndex))
                .map(IndexedWrapper::getObject)
                .filter(Objects::nonNull)
                .forEach(report::addCommandReport);

        return report;
    }

}
