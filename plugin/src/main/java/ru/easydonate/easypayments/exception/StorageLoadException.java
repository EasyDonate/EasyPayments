package ru.easydonate.easypayments.exception;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class StorageLoadException extends Exception {

    public StorageLoadException(@NotNull String message) {
        this(message, null);
    }

    public StorageLoadException(@NotNull Throwable cause) {
        this(cause.getMessage(), cause);
    }

    public StorageLoadException(@NotNull String message, @Nullable Throwable cause) {
        super(message, cause);
    }

}
