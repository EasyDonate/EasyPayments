package ru.easydonate.easypayments.command.exception;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class InitializationException extends RuntimeException {

    public static final InitializationException NO_COMMAND_SPECIFIED = new InitializationException("There are no @Command annotation present!");

    public InitializationException(@NotNull String message, @Nullable Object... args) {
        this(String.format(message, args), (Throwable) null);
    }

    public InitializationException(@NotNull Throwable cause) {
        this(cause.getMessage(), cause);
    }

    public InitializationException(@NotNull String message, @Nullable Throwable cause) {
        super(message, cause);
    }

}
