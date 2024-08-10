package ru.easydonate.easypayments.core.placeholder.bean;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@FunctionalInterface
public interface BeanValueConverter<T> {

    @Nullable T convert(@NotNull String value) throws Exception;

}
