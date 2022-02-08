package ru.easydonate.easypayments.execution.processor.object;

import org.jetbrains.annotations.NotNull;
import ru.easydonate.easypayments.easydonate4j.extension.data.model.EventReportObject;
import ru.easydonate.easypayments.easydonate4j.longpoll.data.model.EventObject;
import ru.easydonate.easypayments.exception.StructureValidationException;

@FunctionalInterface
public interface EventObjectProcessor<E extends EventObject, R extends EventReportObject> {

    @NotNull R processObject(@NotNull E eventObject) throws StructureValidationException;

}
