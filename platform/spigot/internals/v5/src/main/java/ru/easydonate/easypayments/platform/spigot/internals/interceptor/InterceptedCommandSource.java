package ru.easydonate.easypayments.platform.spigot.internals.interceptor;

import lombok.Getter;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_21_R7.command.ServerCommandSender;
import org.bukkit.permissions.Permission;
import org.jspecify.annotations.NonNull;
import ru.easydonate.easypayments.core.Constants;
import ru.easydonate.easypayments.core.interceptor.FeedbackInterceptor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

final class InterceptedCommandSource extends ServerCommandSender implements CommandSource, FeedbackInterceptor {

    private final String username;
    
    @Getter private final List<String> feedbackMessages;

    public InterceptedCommandSource(String username) {
        this.username = username;
        this.feedbackMessages = new ArrayList<>();
    }

    @Override public void sendSystemMessage(@NonNull Component component) {
        feedbackMessages.add(component.getString());
    }

    @Override public boolean acceptsSuccess() {
        return Constants.COMMAND_SENDER_ACCEPTS_SUCCESS;
    }

    @Override public boolean acceptsFailure() {
        return Constants.COMMAND_SENDER_ACCEPTS_FAILURE;
    }

    @Override public boolean shouldInformAdmins() {
        return Constants.COMMAND_SENDER_INFORM_ADMINS;
    }

    @Override public CommandSender getBukkitSender(CommandSourceStack commandSourceStack) {
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