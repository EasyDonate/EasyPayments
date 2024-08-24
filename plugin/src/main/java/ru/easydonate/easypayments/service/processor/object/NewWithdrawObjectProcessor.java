package ru.easydonate.easypayments.service.processor.object;

import org.jetbrains.annotations.NotNull;
import ru.easydonate.easypayments.core.easydonate4j.extension.data.model.object.NewWithdrawReport;
import ru.easydonate.easypayments.core.easydonate4j.longpoll.data.model.object.NewWithdrawEvent;
import ru.easydonate.easypayments.core.exception.StructureValidationException;
import ru.easydonate.easypayments.service.execution.ExecutionService;

import java.util.List;

public final class NewWithdrawObjectProcessor extends EventObjectProcessor<NewWithdrawEvent, NewWithdrawReport> {

    private final ExecutionService executionService;

    public NewWithdrawObjectProcessor(@NotNull ExecutionService executionService) {
        this.executionService = executionService;
    }

    @Override
    public @NotNull NewWithdrawReport processObject(@NotNull NewWithdrawEvent eventObject) throws StructureValidationException {
        eventObject.validate();

        int withdrawId = eventObject.getWithdrawId();
        NewWithdrawReport report = new NewWithdrawReport(withdrawId);
        List<String> commands = eventObject.getCommands();

        // execute commands just now
        executionService.processCommandsKeepSequence(commands).forEach(report::addCommandReport);
        return report;
    }

}
