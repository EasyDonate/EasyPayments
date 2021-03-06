package ru.easydonate.easypayments.execution.processor.object;

import org.jetbrains.annotations.NotNull;
import ru.easydonate.easypayments.easydonate4j.extension.data.model.object.NewRewardReport;
import ru.easydonate.easypayments.easydonate4j.longpoll.data.model.object.NewRewardEvent;
import ru.easydonate.easypayments.exception.StructureValidationException;
import ru.easydonate.easypayments.execution.ExecutionController;

import java.util.List;

public final class NewRewardObjectProcessor extends EventObjectProcessor<NewRewardEvent, NewRewardReport> {

    private final ExecutionController controller;

    public NewRewardObjectProcessor(@NotNull ExecutionController controller) {
        this.controller = controller;
    }

    @Override
    public @NotNull NewRewardReport processObject(@NotNull NewRewardEvent eventObject) throws StructureValidationException {
        eventObject.validate();

        int rewardId = eventObject.getRewardId();
        NewRewardReport report = new NewRewardReport(rewardId);
        List<String> commands = eventObject.getCommands();

        // execute commands just now
        controller.processCommandsKeepSequence(commands).forEach(report::addCommandReport);
        return report;
    }

}
