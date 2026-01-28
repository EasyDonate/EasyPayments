package ru.easydonate.easypayments.platform.spigot.internals.interceptor;

import lombok.Getter;
import net.minecraft.server.v1_13_R1.CommandListenerWrapper;
import net.minecraft.server.v1_13_R1.IChatBaseComponent;
import net.minecraft.server.v1_13_R1.ICommandListener;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_13_R1.command.ServerCommandSender;
import org.bukkit.permissions.Permission;
import ru.easydonate.easypayments.core.Constants;
import ru.easydonate.easypayments.core.interceptor.FeedbackInterceptor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

final class InterceptedCommandListener extends ServerCommandSender implements ICommandListener, FeedbackInterceptor {

    private final String username;

    @Getter private final List<String> feedbackMessages;

    public InterceptedCommandListener(String username) {
        this.username = username;
        this.feedbackMessages = new ArrayList<>();
    }

    public void sendMessage(IChatBaseComponent iChatBaseComponent) {
        feedbackMessages.add(iChatBaseComponent.getString());
    }

    public void sendMessage(IChatBaseComponent iChatBaseComponent, UUID sender) {
        feedbackMessages.add(iChatBaseComponent.getString());
    }

    public boolean a() {
        return Constants.COMMAND_SENDER_ACCEPTS_SUCCESS;
    }

    public boolean b() {
        return Constants.COMMAND_SENDER_ACCEPTS_FAILURE;
    }

    public boolean B_() {
        return Constants.COMMAND_SENDER_INFORM_ADMINS;
    }

    public CommandSender getBukkitSender(CommandListenerWrapper commandListenerWrapper) {
        return this;
    }

    public void sendMessage(String message) {
        feedbackMessages.add(message);
    }

    public void sendMessage(String[] messages) {
        feedbackMessages.addAll(Arrays.asList(messages));
    }

    public String getName() {
        return username;
    }

    public boolean isOp() {
        return true;
    }

    public void setOp(boolean value) {
        // nothing to do here
    }

    public boolean hasPermission(String name) {
        return true;
    }

    public boolean hasPermission(Permission perm) {
        return true;
    }

    public boolean isPermissionSet(String name) {
        return true;
    }

    public boolean isPermissionSet(Permission perm) {
        return true;
    }

}