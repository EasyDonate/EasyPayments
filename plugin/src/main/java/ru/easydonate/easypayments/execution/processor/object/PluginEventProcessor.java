package ru.easydonate.easypayments.execution.processor.object;

import org.jetbrains.annotations.NotNull;
import ru.easydonate.easypayments.easydonate4j.extension.data.model.plugin.PluginEventReport;
import ru.easydonate.easypayments.easydonate4j.longpoll.data.model.plugin.PluginEvent;

@FunctionalInterface
public interface PluginEventProcessor<P extends PluginEvent> {

    @NotNull PluginEventReport process(@NotNull P pluginEvent) throws Exception;

}
