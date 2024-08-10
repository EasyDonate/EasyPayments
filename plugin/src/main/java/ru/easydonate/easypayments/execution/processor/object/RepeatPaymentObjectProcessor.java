package ru.easydonate.easypayments.execution.processor.object;

import org.jetbrains.annotations.NotNull;
import ru.easydonate.easypayments.core.easydonate4j.extension.data.model.object.RepeatPaymentReport;
import ru.easydonate.easypayments.core.easydonate4j.longpoll.data.model.object.PurchasedProduct;
import ru.easydonate.easypayments.core.easydonate4j.longpoll.data.model.object.RepeatPaymentEvent;
import ru.easydonate.easypayments.core.exception.StructureValidationException;
import ru.easydonate.easypayments.execution.ExecutionController;

import java.util.List;
import java.util.stream.Collectors;

public final class RepeatPaymentObjectProcessor extends EventObjectProcessor<RepeatPaymentEvent, RepeatPaymentReport> {

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

        List<String> commands = products.stream()
                .map(PurchasedProduct::getCommands)
                .flatMap(List::stream)
                .collect(Collectors.toList());

        // execute commands just now
        controller.processCommandsKeepSequence(commands).forEach(report::addCommandReport);
        return report;
    }

}
