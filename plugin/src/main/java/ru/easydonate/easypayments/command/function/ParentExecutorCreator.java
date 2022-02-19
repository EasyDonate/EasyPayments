package ru.easydonate.easypayments.command.function;

import org.jetbrains.annotations.NotNull;
import ru.easydonate.easypayments.command.CommandExecutor;
import ru.easydonate.easypayments.command.Executor;
import ru.easydonate.easypayments.command.exception.InitializationException;
import ru.easydonate.easypayments.config.Messages;

@FunctionalInterface
public interface ParentExecutorCreator<T extends Executor> {

    @NotNull T create(@NotNull CommandExecutor parent, @NotNull Messages messages) throws InitializationException;

}
