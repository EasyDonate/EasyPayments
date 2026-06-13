package ru.easydonate.easypayments.platform.spigot.unrelocated.v1.interceptor;

import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.command.ProxiedNativeCommandSender;
import org.jspecify.annotations.NonNull;
import ru.easydonate.easypayments.core.interceptor.FeedbackInterceptor;

import java.util.List;

final class InterceptedProxiedCommandSender extends ProxiedNativeCommandSender implements FeedbackInterceptor {

    public InterceptedProxiedCommandSender(
            @NonNull InterceptedCommandSourceStack original,
            @NonNull CommandSender sender
    ) {
        super(original, sender, sender);
    }

    @Override public @NonNull InterceptedCommandSourceStack getHandle() {
        return (InterceptedCommandSourceStack) super.getHandle();
    }

    @Override public @NonNull List<String> getFeedbackMessages() {
        return getHandle().getFeedbackMessages();
    }

}