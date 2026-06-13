package ru.easydonate.easypayments.platform.spigot.unrelocated.v1.interceptor;

import lombok.Getter;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.rcon.RconConsoleSource;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.command.RemoteConsoleCommandSender;
import org.bukkit.craftbukkit.command.ServerCommandSender;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;
import ru.easydonate.easypayments.core.Constants;
import ru.easydonate.easypayments.core.interceptor.FeedbackInterceptor;

import java.net.SocketAddress;
import java.util.*;

import static ru.easydonate.easypayments.core.interceptor.InterceptorFactoryBase.LOOPBACK_ADDRESS;

final class InterceptedRconCommandSource extends RconConsoleSource implements RemoteConsoleCommandSender, FeedbackInterceptor {

    private final @NotNull MinecraftServer server;
    private final @NotNull BukkitCommandSender delegate;

    public InterceptedRconCommandSource(@NotNull MinecraftServer server, @NotNull String username) {
        super(server, LOOPBACK_ADDRESS);
        this.server = server;
        this.delegate = new BukkitCommandSender(username);
    }

    @Override public @NotNull List<String> getFeedbackMessages() {
        return delegate.getFeedbackMessages();
    }

    @Override public @NotNull SocketAddress getAddress() {
        return LOOPBACK_ADDRESS;
    }

    @Override public void prepareForCommand() {
        // nothing to do here
    }

    @Override public @NonNull String getCommandResponse() {
        return String.join("\n", delegate.feedbackMessages);
    }

    @Override public @NonNull InterceptedCommandSourceStack createCommandSourceStack() {
        return new InterceptedCommandSourceStack(this, server.overworld(), delegate.username);
    }

    @Override public CommandSender getBukkitSender(CommandSourceStack commandSourceStack) {
        return this;
    }

    @Override public void sendMessage(String message) {
        delegate.sendMessage(message);
    }

    @Override public void sendMessage(@NonNull @NotNull String... messages) {
        delegate.sendMessage(messages);
    }

    @Override public void sendMessage(@Nullable UUID sender, @NotNull String message) {
        delegate.sendMessage(sender, message);
    }

    @Override public void sendMessage(@Nullable UUID sender, @NonNull @NotNull String... messages) {
        delegate.sendMessage(sender, messages);
    }

    @Override public @NotNull Server getServer() {
        return delegate.getServer();
    }

    @Override public @NotNull String getName() {
        return delegate.getName();
    }

    @Override public @NotNull Spigot spigot() {
        return delegate.spigot();
    }

    @Override public void sendSystemMessage(@NonNull Component component) {
        delegate.feedbackMessages.add(component.getString());
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

    @Override public boolean isPermissionSet(@NotNull String name) {
        return delegate.isPermissionSet(name);
    }

    @Override public boolean isPermissionSet(@NotNull Permission perm) {
        return delegate.isPermissionSet(perm);
    }

    @Override public boolean hasPermission(@NotNull String name) {
        return delegate.hasPermission(name);
    }

    @Override public boolean hasPermission(@NotNull Permission perm) {
        return delegate.hasPermission(perm);
    }

    @Override public @NotNull PermissionAttachment addAttachment(@NotNull Plugin plugin, @NotNull String name, boolean value) {
        return delegate.addAttachment(plugin, name, value);
    }

    @Override public @NotNull PermissionAttachment addAttachment(@NotNull Plugin plugin) {
        return delegate.addAttachment(plugin);
    }

    @Override public @Nullable PermissionAttachment addAttachment(@NotNull Plugin plugin, @NotNull String name, boolean value, int ticks) {
        return delegate.addAttachment(plugin, name, value, ticks);
    }

    @Override public @Nullable PermissionAttachment addAttachment(@NotNull Plugin plugin, int ticks) {
        return delegate.addAttachment(plugin, ticks);
    }

    @Override public void removeAttachment(@NotNull PermissionAttachment attachment) {
        delegate.removeAttachment(attachment);
    }

    @Override public void recalculatePermissions() {
        delegate.recalculatePermissions();
    }

    @Override public @NotNull Set<PermissionAttachmentInfo> getEffectivePermissions() {
        return delegate.getEffectivePermissions();
    }

    @Override public boolean isOp() {
        return delegate.isOp();
    }

    @Override public void setOp(boolean value) {
        delegate.setOp(value);
    }

    @Getter
    final static class BukkitCommandSender extends ServerCommandSender implements FeedbackInterceptor {

        private final @NotNull String username;
        private final @NotNull List<String> feedbackMessages;

        public BukkitCommandSender(@NotNull String username) {
            this.username = username;
            this.feedbackMessages = new ArrayList<>();
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

}
