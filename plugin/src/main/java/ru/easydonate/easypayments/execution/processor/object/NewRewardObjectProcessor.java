package ru.easydonate.easypayments.execution.processor.object;

import org.jetbrains.annotations.NotNull;
import ru.easydonate.easypayments.easydonate4j.extension.data.model.object.NewRewardReport;
import ru.easydonate.easypayments.easydonate4j.longpoll.data.model.object.NewRewardEvent;
import ru.easydonate.easypayments.exception.StructureValidationException;
import ru.easydonate.easypayments.execution.ExecutionController;
import ru.easydonate.easypayments.execution.IndexedWrapper;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

public final class NewRewardObjectProcessor implements EventObjectProcessor<NewRewardEvent, NewRewardReport> {

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
        AtomicInteger indexer = new AtomicInteger();
        commands.stream()
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
