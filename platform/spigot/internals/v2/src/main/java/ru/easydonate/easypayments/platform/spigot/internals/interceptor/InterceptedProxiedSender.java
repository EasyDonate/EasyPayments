package ru.easydonate.easypayments.platform.spigot.internals.interceptor;

import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_13_R1.command.ProxiedNativeCommandSender;
import ru.easydonate.easypayments.core.interceptor.FeedbackInterceptor;

import java.util.List;

final class InterceptedProxiedSender extends ProxiedNativeCommandSender implements FeedbackInterceptor {

    public InterceptedProxiedSender(InterceptedCommandListenerWrapper original, CommandSender sender) {
        super(original, sender, sender);
    }

    @Override
    public InterceptedCommandListenerWrapper getHandle() {
        return (InterceptedCommandListenerWrapper) super.getHandle();
    }

    @Override
    public List<String> getFeedbackMessages() {
        return getHandle().getFeedbackMessages();
    }

}