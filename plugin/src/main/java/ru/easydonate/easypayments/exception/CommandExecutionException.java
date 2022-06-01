package ru.easydonate.easypayments.exception;

import lombok.Getter;
import org.bukkit.ChatColor;
import org.jetbrains.annotations.NotNull;
import ru.easydonate.easypayments.execution.interceptor.FeedbackInterceptor;

@Getter
public final class CommandExecutionException extends RuntimeException {

    private static final String MESSAGE_FORMAT = "Couldn't correctly execute command '%s'";

    private final String command;
    private final FeedbackInterceptor executor;

    public CommandExecutionException(@NotNull String command, @NotNull FeedbackInterceptor executor, @NotNull Throwable cause) {
        super(String.format(MESSAGE_FORMAT, ChatColor.stripColor(command)), cause);
        this.command = command;
        this.executor = executor;
    }

}
