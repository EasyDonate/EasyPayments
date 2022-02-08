package ru.easydonate.easypayments.nms.provider.v1_8_R1.interceptor;

import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_8_R1.command.ProxiedNativeCommandSender;
import ru.easydonate.easypayments.execution.interceptor.FeedbackInterceptor;

import java.util.List;

public final class InterceptedProxiedSender extends ProxiedNativeCommandSender implements FeedbackInterceptor {
    public InterceptedProxiedSender(InterceptedCommandListener orig, CommandSender sender) {
        super(orig, sender, sender);
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
