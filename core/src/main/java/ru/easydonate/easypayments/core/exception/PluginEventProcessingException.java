package ru.easydonate.easypayments.core.exception;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.easydonate.easypayments.core.easydonate4j.longpoll.data.model.plugin.PluginEvent;

@Getter
public final class PluginEventProcessingException extends RuntimeException {

    private static final String MESSAGE_FORMAT = "An error occured during the '%s' plugin event processing: %s";

    private final PluginEvent pluginEvent;

    public PluginEventProcessingException(@NotNull PluginEvent pluginEvent, @NotNull String message) {
        this(pluginEvent, message, null);
    }

    public PluginEventProcessingException(@NotNull PluginEvent pluginEvent, @NotNull Throwable cause) {
        this(pluginEvent, cause.getMessage(), cause);
    }

    public PluginEventProcessingException(@NotNull PluginEvent pluginEvent, @NotNull String message, @Nullable Throwable cause) {
        super(String.format(MESSAGE_FORMAT, pluginEvent.getPluginType(), message), cause);
        this.pluginEvent = pluginEvent;
    }

}
