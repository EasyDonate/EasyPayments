package ru.easydonate.easypayments.core.util;

import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;

@UtilityClass
public class ThrowableCauseFinder {

    public @NotNull Throwable findLastCause(@NotNull Throwable source) {
        Throwable cause = source;
        while (cause != null && cause.getCause() != null)
            cause = cause.getCause();
        return cause;
    }

}
