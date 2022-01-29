package ru.easydonate.easypayments.nms.proxy.v1_18_R1.interceptor;

import lombok.Getter;
import net.minecraft.commands.CommandListenerWrapper;
import net.minecraft.commands.ICommandListener;
import net.minecraft.network.chat.IChatBaseComponent;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_18_R1.command.ServerCommandSender;
import org.bukkit.permissions.Permission;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Getter
final class InterceptedCommandListener extends ServerCommandSender implements ICommandListener {

    private final String username;
    private final List<String> feedbackMessages;

    public InterceptedCommandListener(String username) {
        this.username = username;
        this.feedbackMessages = new ArrayList<>();
    }

    @Override
    public void a(IChatBaseComponent iChatBaseComponent, UUID uuid) {
        feedbackMessages.add(iChatBaseComponent.getString());
    }

    @Override
    public boolean i_() {
        return true;
    }

    @Override
    public boolean j_() {
        return true;
    }

    @Override
    public boolean F_() {
        return true;
    }

    @Override
    public CommandSender getBukkitSender(CommandListenerWrapper commandListenerWrapper) {
        return this;
    }

    @Override
    public void sendMessage(String message) {
        feedbackMessages.add(message);
    }

    @Override
    public void sendMessage(String[] messages) {
        feedbackMessages.addAll(Arrays.asList(messages));
    }

    @Override
    public String getName() {
        return username;
    }

    @Override
    public boolean isOp() {
        return true;
    }

    @Override
    public void setOp(boolean value) {
    }

    @Override
    public boolean hasPermission(String name) {
        return true;
    }

    @Override
    public boolean hasPermission(Permission perm) {
        return true;
    }

    @Override
    public boolean isPermissionSet(String name) {
        return true;
    }

    @Override
    public boolean isPermissionSet(Permission perm) {
        return true;
    }

}
