package ru.easydonate.easypayments.platform.spigot.internals.interceptor;

import lombok.Getter;
import net.minecraft.server.v1_13_R1.CommandListenerWrapper;
import net.minecraft.server.v1_13_R1.IChatBaseComponent;
import net.minecraft.server.v1_13_R1.ICommandListener;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_13_R1.command.ServerCommandSender;
import org.bukkit.permissions.Permission;
import org.jetbrains.annotations.NotNull;
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

    @Override public void sendMessage(@NotNull IChatBaseComponent iChatBaseComponent) {
        feedbackMessages.add(iChatBaseComponent.getString());
    }

    public void sendMessage(@NotNull IChatBaseComponent iChatBaseComponent, UUID sender) {
        feedbackMessages.add(iChatBaseComponent.getString());
    }

    @Override public boolean a() {
        return Constants.COMMAND_SENDER_ACCEPTS_SUCCESS;
    }

    @Override public boolean b() {
        return Constants.COMMAND_SENDER_ACCEPTS_FAILURE;
    }

    @Override public boolean B_() {
        return Constants.COMMAND_SENDER_INFORM_ADMINS;
    }

    @Override public CommandSender getBukkitSender(CommandListenerWrapper commandListenerWrapper) {
        return this;
    }

    @Override public void sendMessage(String message) {
        feedbackMessages.add(message);
    }

    @Override public void sendMessage(String[] messages) {
        feedbackMessages.addAll(Arrays.asList(messages));
    }

    @Override public String getName() {
        return username;
    }

    @Override public boolean isOp() {
        return true;
    }

    @Override public void setOp(boolean value) {
        // nothing to do here
    }

    @Override public boolean hasPermission(String name) {
        return true;
    }

    @Override public boolean hasPermission(Permission perm) {
        return true;
    }

    @Override public boolean isPermissionSet(String name) {
        return true;
    }

    @Override public boolean isPermissionSet(Permission perm) {
        return true;
    }

}