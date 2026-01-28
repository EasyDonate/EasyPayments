package ru.easydonate.easypayments.platform.paper.internals.v2.interceptor;

import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.command.ProxiedNativeCommandSender;
import ru.easydonate.easypayments.core.interceptor.FeedbackInterceptor;

import java.util.List;

final class InterceptedProxiedSender extends ProxiedNativeCommandSender implements FeedbackInterceptor {

    public InterceptedProxiedSender(InterceptedCommandSourceStack original, CommandSender sender) {
        super(original, sender, sender);
    }

    @Override public InterceptedCommandSourceStack getHandle() {
        return (InterceptedCommandSourceStack) super.getHandle();
    }

    @Override public List<String> getFeedbackMessages() {
        return getHandle().getFeedbackMessages();
    }

}