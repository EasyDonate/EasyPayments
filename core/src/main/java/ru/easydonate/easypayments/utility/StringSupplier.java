package ru.easydonate.easypayments.utility;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.easydonate.easypayments.config.Messages;

import java.util.function.Supplier;

@FunctionalInterface
public interface StringSupplier extends Supplier<String> {

    static @NotNull StringSupplier constant(@NotNull String content) {
        return () -> content;
    }

    static @NotNull StringSupplier messageKey(@NotNull Messages messages, @NotNull String key, @Nullable Object... args) {
        return () -> messages.get(key, args);
    }

    @Override
    String get();

}
