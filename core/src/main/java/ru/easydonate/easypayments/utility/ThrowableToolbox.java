package ru.easydonate.easypayments.utility;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class ThrowableToolbox {

    public static @Nullable Throwable findLastCause(@NotNull Throwable source) {
        Throwable cause = source;
        while(cause != null && cause.getCause() != null) {
            cause = cause.getCause();
        }
        return cause;
    }

}
