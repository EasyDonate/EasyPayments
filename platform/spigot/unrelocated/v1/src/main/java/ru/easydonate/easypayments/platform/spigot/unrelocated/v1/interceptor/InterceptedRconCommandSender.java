package ru.easydonate.easypayments.platform.spigot.unrelocated.v1.interceptor;

import org.bukkit.craftbukkit.command.CraftRemoteConsoleCommandSender;
import org.jetbrains.annotations.NotNull;
import ru.easydonate.easypayments.core.interceptor.FeedbackInterceptor;

import java.util.List;

final class InterceptedRconCommandSender extends CraftRemoteConsoleCommandSender implements FeedbackInterceptor {

    public InterceptedRconCommandSender(@NotNull InterceptedRconCommandSource rconCommandSource) {
        super(rconCommandSource);
    }

    @Override public @NotNull InterceptedRconCommandSource getListener() {
        return (InterceptedRconCommandSource) super.getListener();
    }

    @Override public @NotNull String getName() {
        return getListener().getName();
    }

    @Override public @NotNull List<String> getFeedbackMessages() {
        return getListener().getFeedbackMessages();
    }

}
