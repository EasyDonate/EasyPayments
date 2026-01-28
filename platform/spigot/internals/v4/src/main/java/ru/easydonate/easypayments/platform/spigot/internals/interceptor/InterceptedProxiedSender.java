package ru.easydonate.easypayments.platform.spigot.internals.interceptor;

import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_19_R1.command.ProxiedNativeCommandSender;
import org.jetbrains.annotations.NotNull;
import ru.easydonate.easypayments.core.interceptor.FeedbackInterceptor;

import java.util.List;

final class InterceptedProxiedSender extends ProxiedNativeCommandSender implements FeedbackInterceptor {

    public InterceptedProxiedSender(InterceptedCommandSourceStack original, CommandSender sender) {
        super(original, sender, sender);
    }

    @Override public InterceptedCommandSourceStack getHandle() {
        return (InterceptedCommandSourceStack) super.getHandle();
    }

    @Override public @NotNull List<String> getFeedbackMessages() {
        return getHandle().getFeedbackMessages();
    }

}