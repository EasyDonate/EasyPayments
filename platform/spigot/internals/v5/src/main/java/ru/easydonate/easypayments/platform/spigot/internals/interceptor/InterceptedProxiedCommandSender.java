package ru.easydonate.easypayments.platform.spigot.internals.interceptor;

import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_20_R3.command.ProxiedNativeCommandSender;
import org.jetbrains.annotations.NotNull;
import ru.easydonate.easypayments.core.interceptor.FeedbackInterceptor;

import java.util.List;

final class InterceptedProxiedCommandSender extends ProxiedNativeCommandSender implements FeedbackInterceptor {

    public InterceptedProxiedCommandSender(
            @NotNull InterceptedCommandSourceStack original,
            @NotNull CommandSender sender
    ) {
        super(original, sender, sender);
    }

    @Override public @NotNull InterceptedCommandSourceStack getHandle() {
        return (InterceptedCommandSourceStack) super.getHandle();
    }

    @Override public @NotNull List<String> getFeedbackMessages() {
        return getHandle().getFeedbackMessages();
    }

}