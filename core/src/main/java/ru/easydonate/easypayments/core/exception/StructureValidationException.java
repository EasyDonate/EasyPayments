package ru.easydonate.easypayments.core.exception;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Getter
public final class StructureValidationException extends RuntimeException {

    private static final String MESSAGE_FORMAT = "Оbject structure validation fail (%s): %s";

    private final Object instance;

    public StructureValidationException(@NotNull Object instance, @NotNull String message, @Nullable Object... args) {
        super(String.format(MESSAGE_FORMAT, String.format(message, args), instance));
        this.instance = instance;
    }

}
