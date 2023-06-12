package ru.easydonate.easypayments.nms.provider.v1_20_R1.interceptor;

import lombok.Getter;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_20_R1.command.ServerCommandSender;
import org.bukkit.permissions.Permission;
import org.jetbrains.annotations.NotNull;
import ru.easydonate.easypayments.Constants;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Getter
final class InterceptedCommandListener extends ServerCommandSender implements CommandSource {

    private final String username;
    private final List<String> feedbackMessages;

    public InterceptedCommandListener(String username) {
        this.username = username;
        this.feedbackMessages = new ArrayList<>();
    }

    @Override
    public void sendSystemMessage(Component component) {
        feedbackMessages.add(component.getString());
    }

    @Override
    public boolean acceptsSuccess() {
        return Constants.COMMAND_SENDER_ACCEPTS_SUCCESS;
    }

    @Override
    public boolean acceptsFailure() {
        return Constants.COMMAND_SENDER_ACCEPTS_FAILURE;
    }

    @Override
    public boolean shouldInformAdmins() {
        return Constants.COMMAND_SENDER_INFORM_ADMINS;
    }

    @Override
    public CommandSender getBukkitSender(CommandSourceStack commandSourceStack) {
        return this;
    }

    @Override
    public void sendMessage(@NotNull String message) {
        feedbackMessages.add(message);
    }

    @Override
    public void sendMessage(String[] messages) {
        feedbackMessages.addAll(Arrays.asList(messages));
    }

    @Override
    public @NotNull String getName() {
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
