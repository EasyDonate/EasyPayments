package ru.easydonate.easypayments.command.exception;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class ExecutionException extends Exception {

    public ExecutionException(@NotNull String message, @Nullable Object... args) {
        this(String.format(message, args), (Throwable) null);
    }

    public ExecutionException(@NotNull Throwable cause) {
        this(cause.getMessage(), cause);
    }

    public ExecutionException(@NotNull String message, @Nullable Throwable cause) {
        super(message, cause);
    }

}
