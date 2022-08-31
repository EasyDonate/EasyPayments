package ru.easydonate.easypayments.execution.processor.object;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.easydonate.easypayments.easydonate4j.PluginEventType;
import ru.easydonate.easypayments.easydonate4j.extension.data.model.EventReportObject;
import ru.easydonate.easypayments.easydonate4j.extension.data.model.plugin.PluginEventReport;
import ru.easydonate.easypayments.easydonate4j.longpoll.data.model.EventObject;
import ru.easydonate.easypayments.easydonate4j.longpoll.data.model.object.PurchasedProduct;
import ru.easydonate.easypayments.easydonate4j.longpoll.data.model.plugin.PluginEvent;
import ru.easydonate.easypayments.exception.PluginEventProcessingException;
import ru.easydonate.easypayments.exception.StructureValidationException;

import java.util.*;
import java.util.stream.IntStream;

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

    protected List<String> getAllCommands(PurchasedProduct product) {
        if (product.getCount() > 1)
            return product.getCommands();

        return IntStream.range(0, product.getCount())
                .mapToObj(i -> product.getCommands())
                .collect(ArrayList::new, List::addAll, List::addAll);
    }

    public @NotNull R processPluginEvents(@NotNull E eventObject, @NotNull R eventReportObject) {
        if(eventObject == null || eventReportObject == null)
            return eventReportObject;

        if(eventObject.hasPluginEvents()) {
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
        if(pluginType.isUnknown())
            return null;

        PluginEventProcessor<P> processor = (PluginEventProcessor<P>) pluginEventProcessors.get(pluginType);
        if(processor == null)
            return null;

        try {
            return processor.process((P) pluginEvent);
        } catch (Exception ex) {
            throw new PluginEventProcessingException(pluginEvent, ex);
        }
    }

}
