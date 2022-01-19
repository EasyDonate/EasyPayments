package ru.easydonate.easypayments.config;

import org.jetbrains.annotations.NotNull;
import ru.easydonate.easypayments.exception.ConfigurationValidationException;

@FunctionalInterface
public interface Validator<C extends AbstractConfiguration<C>> {

    void validate(@NotNull C configuration) throws ConfigurationValidationException;

}
