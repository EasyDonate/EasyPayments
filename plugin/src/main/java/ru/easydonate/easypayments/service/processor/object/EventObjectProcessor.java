package ru.easydonate.easypayments.service.processor.object;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.easydonate.easypayments.core.easydonate4j.PluginEventType;
import ru.easydonate.easypayments.core.easydonate4j.extension.data.model.EventReportObject;
import ru.easydonate.easypayments.core.easydonate4j.extension.data.model.plugin.PluginEventReport;
import ru.easydonate.easypayments.core.easydonate4j.longpoll.data.model.EventObject;
import ru.easydonate.easypayments.core.easydonate4j.longpoll.data.model.plugin.PluginEvent;
import ru.easydonate.easypayments.core.exception.PluginEventProcessingException;
import ru.easydonate.easypayments.core.exception.StructureValidationException;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public abstract class EventObjectProcessor<E extends EventObject, R extends EventReportObject> {

    protected final Map<PluginEventType, PluginEventProcessor<?>> pluginEventProcessors;

    protected EventObjectProcessor() {
        this.pluginEventProcessors = new LinkedHashMap<>();
    }

    public abstract @NotNull R processObject(@NotNull E eventObject) throws StructureValidationException;

    protected <P extends PluginEvent> void registerPluginEventProcessor(
            @NotNull PluginEventType pluginType,
            @NotNull PluginEventProcessor<P> pluginEventProcessor
    ) {
        pluginEventProcessors.put(pluginType, pluginEventProcessor);
    }

    public @NotNull R processPluginEvents(@NotNull E eventObject, @NotNull R eventReportObject) {
        if (eventObject == null || eventReportObject == null)
            return eventReportObject;

        if (eventObject.hasPluginEvents()) {
            // add support for multiple plugins processing when that will be needed
            // parallel processing may be?
            eventObject.getPluginEvents().stream()
                    .map(this::processPluginEvent)
                    .filter(Objects::nonNull)
                    .forEach(eventReportObject::addPluginEventReport);
        }

        return eventReportObject;
    }

    @SuppressWarnings("unchecked")
    private <P extends PluginEvent> @Nullable PluginEventReport processPluginEvent(@NotNull PluginEvent pluginEvent) {
        PluginEventType pluginType = pluginEvent.getPluginType();
        if (pluginType.isUnknown())
            return null;

        PluginEventProcessor<P> processor = (PluginEventProcessor<P>) pluginEventProcessors.get(pluginType);
        if (processor == null)
            return null;

        try {
            return processor.process((P) pluginEvent);
        } catch (Exception ex) {
            throw new PluginEventProcessingException(pluginEvent, ex);
        }
    }

}
