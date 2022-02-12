package ru.easydonate.easypayments.execution.processor.object;

import org.jetbrains.annotations.NotNull;
import ru.easydonate.easypayments.easydonate4j.extension.data.model.object.NewWithdrawReport;
import ru.easydonate.easypayments.easydonate4j.longpoll.data.model.object.NewWithdrawEvent;
import ru.easydonate.easypayments.exception.StructureValidationException;
import ru.easydonate.easypayments.execution.ExecutionController;

import java.util.List;

public final class NewWithdrawObjectProcessor extends EventObjectProcessor<NewWithdrawEvent, NewWithdrawReport> {

    private final ExecutionController controller;

    public NewWithdrawObjectProcessor(@NotNull ExecutionController controller) {
        this.controller = controller;
    }

    @Override
    public @NotNull NewWithdrawReport processObject(@NotNull NewWithdrawEvent eventObject) throws StructureValidationException {
        eventObject.validate();

        int paymentId = eventObject.getPaymentId();
        NewWithdrawReport report = new NewWithdrawReport(paymentId);
        List<String> commands = eventObject.getCommands();

        // execute commands just now
        controller.processCommandsKeepSequence(commands).forEach(report::addCommandReport);
        return report;
    }

}
