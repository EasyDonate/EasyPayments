package ru.easydonate.easypayments.service.processor.object;

import org.jetbrains.annotations.NotNull;
import ru.easydonate.easypayments.core.easydonate4j.extension.data.model.object.NewRewardReport;
import ru.easydonate.easypayments.core.easydonate4j.longpoll.data.model.object.NewRewardEvent;
import ru.easydonate.easypayments.core.exception.StructureValidationException;
import ru.easydonate.easypayments.service.execution.ExecutionService;

import java.util.List;

public final class NewRewardObjectProcessor extends EventObjectProcessor<NewRewardEvent, NewRewardReport> {

    private final ExecutionService executionService;

    public NewRewardObjectProcessor(@NotNull ExecutionService executionService) {
        this.executionService = executionService;
    }

    @Override
    public @NotNull NewRewardReport processObject(@NotNull NewRewardEvent eventObject) throws StructureValidationException {
        eventObject.validate();

        int rewardId = eventObject.getRewardId();
        NewRewardReport report = new NewRewardReport(rewardId);
        List<String> commands = eventObject.getCommands();

        // execute commands just now
        executionService.processCommandsKeepSequence(commands).forEach(report::addCommandReport);
        return report;
    }

}
