package ru.easydonate.easypayments.utility;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ForwardingFunction<T> implements Function<T, T> {

    public static <T> @NotNull ForwardingFunction<T> create() {
        return new ForwardingFunction<>();
    }

    @Override
    public T apply(T object) {
        return object;
    }

}
