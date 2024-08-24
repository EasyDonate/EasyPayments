package ru.easydonate.easypayments.service.processor.update;

import org.jetbrains.annotations.NotNull;
import ru.easydonate.easypayments.core.easydonate4j.extension.data.model.EventReportObject;
import ru.easydonate.easypayments.core.easydonate4j.extension.data.model.EventUpdateReport;
import ru.easydonate.easypayments.core.easydonate4j.longpoll.data.model.EventObject;
import ru.easydonate.easypayments.core.easydonate4j.longpoll.data.model.EventUpdate;
import ru.easydonate.easypayments.core.exception.StructureValidationException;

@FunctionalInterface
public interface EventUpdateProcessor<E extends EventObject, R extends EventReportObject> {

    @NotNull EventUpdateReport<R> processUpdate(@NotNull EventUpdate<E> eventUpdate) throws StructureValidationException;

}
