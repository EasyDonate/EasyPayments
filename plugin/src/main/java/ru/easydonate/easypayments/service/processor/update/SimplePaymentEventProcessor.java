package ru.easydonate.easypayments.service.processor.update;

import org.jetbrains.annotations.NotNull;
import ru.easydonate.easypayments.core.easydonate4j.EventType;
import ru.easydonate.easypayments.core.easydonate4j.extension.data.model.EventReportObject;
import ru.easydonate.easypayments.core.easydonate4j.extension.data.model.EventUpdateReport;
import ru.easydonate.easypayments.core.easydonate4j.longpoll.data.model.EventObject;
import ru.easydonate.easypayments.core.easydonate4j.longpoll.data.model.EventUpdate;
import ru.easydonate.easypayments.core.exception.StructureValidationException;
import ru.easydonate.easypayments.service.LongPollEventDispatcher;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public final class SimplePaymentEventProcessor<E extends EventObject, R extends EventReportObject> implements EventUpdateProcessor<E, R> {

    private final LongPollEventDispatcher dispatcher;

    public SimplePaymentEventProcessor(@NotNull LongPollEventDispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    @SuppressWarnings("unchecked")
    @Override
    public @NotNull EventUpdateReport<R> processUpdate(
            @NotNull EventUpdate<E> eventUpdate
    ) throws StructureValidationException {
        eventUpdate.validate();

        EventType eventType = eventUpdate.getEventType();
        List<E> eventObjects = eventUpdate.getEventObjects();
        EventUpdateReport<R> report = eventUpdate.createReport();

        eventObjects.parallelStream()
                .map(object -> dispatcher.processEventObject(eventType, object))
                .map(CompletableFuture::join)
                .map(object -> (R) object)
                .forEach(report::addObject);

        return report;
    }

}
