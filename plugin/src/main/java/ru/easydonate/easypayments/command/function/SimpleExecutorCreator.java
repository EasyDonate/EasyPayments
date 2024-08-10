package ru.easydonate.easypayments.command.function;

import org.jetbrains.annotations.NotNull;
import ru.easydonate.easypayments.command.Executor;
import ru.easydonate.easypayments.command.exception.InitializationException;
import ru.easydonate.easypayments.core.config.localized.Messages;

@FunctionalInterface
public interface SimpleExecutorCreator<T extends Executor> {

    @NotNull T create(@NotNull Messages messages) throws InitializationException;

}
