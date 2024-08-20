package ru.easydonate.easypayments.platform.spigot.interceptor;

import lombok.Getter;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_17_R1.command.ServerCommandSender;
import org.bukkit.permissions.Permission;
import ru.easydonate.easypayments.core.Constants;
import ru.easydonate.easypayments.core.interceptor.FeedbackInterceptor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

final class InterceptedCommandSource extends ServerCommandSender implements CommandSource, FeedbackInterceptor {

    private final String username;
    
    @Getter private final List<String> feedbackMessages;

    public InterceptedCommandSource(String username) {
        this.username = username;
        this.feedbackMessages = new ArrayList<>();
    }

    public void sendMessage(Component component, UUID sender) {
        feedbackMessages.add(component.getString());
    }

    public boolean acceptsSuccess() {
        return Constants.COMMAND_SENDER_ACCEPTS_SUCCESS;
    }

    public boolean acceptsFailure() {
        return Constants.COMMAND_SENDER_ACCEPTS_FAILURE;
    }

    public boolean shouldInformAdmins() {
        return Constants.COMMAND_SENDER_INFORM_ADMINS;
    }

    public CommandSender getBukkitSender(CommandSourceStack commandSourceStack) {
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