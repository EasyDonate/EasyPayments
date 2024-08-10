package ru.easydonate.easypayments.core.config;

import ru.easydonate.easypayments.core.exception.ConfigurationValidationException;

@FunctionalInterface
public interface ConfigurationValidator {

    void validate(Configuration configuration) throws ConfigurationValidationException;

}
