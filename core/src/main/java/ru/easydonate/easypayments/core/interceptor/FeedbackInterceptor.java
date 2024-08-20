package ru.easydonate.easypayments.core.interceptor;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface FeedbackInterceptor {

    default @NotNull CommandSender getCommandSender() {
        if (this instanceof CommandSender)
            return (CommandSender) this;

        throw new UnsupportedOperationException("This feedback interceptor isn't an instance of CommandSender!");
    }

    @NotNull List<String> getFeedbackMessages();

}
