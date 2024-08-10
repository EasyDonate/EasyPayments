package ru.easydonate.easypayments.core.exception;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class ConfigurationValidationException extends RuntimeException {

    public ConfigurationValidationException(@NotNull String message) {
        this(message, null);
    }

    public ConfigurationValidationException(@NotNull Throwable cause) {
        this(cause.getMessage(), cause);
    }

    public ConfigurationValidationException(@NotNull String message, @Nullable Throwable cause) {
        super(message, cause);
    }

}
