package ru.easydonate.easypayments.platform.spigot.nms.interceptor;

import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_8_R1.command.ProxiedNativeCommandSender;
import ru.easydonate.easypayments.core.interceptor.FeedbackInterceptor;

import java.util.List;

final class InterceptedProxiedSender extends ProxiedNativeCommandSender implements FeedbackInterceptor {

    public InterceptedProxiedSender(InterceptedCommandListener original, CommandSender sender) {
        super(original, sender, sender);
    }

    @Override
    public InterceptedCommandListener getHandle() {
        return (InterceptedCommandListener) super.getHandle();
    }

    @Override
    public List<String> getFeedbackMessages() {
        return getHandle().getFeedbackMessages();
    }

}