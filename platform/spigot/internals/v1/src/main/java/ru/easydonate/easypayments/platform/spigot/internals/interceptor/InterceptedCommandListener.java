package ru.easydonate.easypayments.platform.spigot.internals.interceptor;

import lombok.Getter;
import net.minecraft.server.v1_8_R1.*;
import org.bukkit.craftbukkit.v1_8_R1.command.ServerCommandSender;
import org.bukkit.permissions.Permission;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.easydonate.easypayments.core.Constants;
import ru.easydonate.easypayments.core.interceptor.FeedbackInterceptor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

final class InterceptedCommandListener extends ServerCommandSender implements ICommandListener, FeedbackInterceptor {

    private static final Vec3D POSITION = new Vec3D(0D, 0D, 0D);

    private final MinecraftServer minecraftServer;
    private final String username;

    @Getter private final List<String> feedbackMessages;

    public InterceptedCommandListener(MinecraftServer minecraftServer, String username) {
        this.minecraftServer = minecraftServer;
        this.username = username;
        this.feedbackMessages = new ArrayList<>();
    }

    @Override public boolean getSendCommandFeedback() {
        return Constants.COMMAND_SENDER_INFORM_ADMINS;
    }

    @Override public void a(EnumCommandResult enumCommandResult, int i) {
        // nothing to do here
    }

    @Override public void sendMessage(@NotNull IChatBaseComponent iChatBaseComponent) {
        feedbackMessages.add(iChatBaseComponent.c());
    }

    @Override public boolean a(int i, String s) {
        return true;
    }

    @Override public BlockPosition getChunkCoordinates() {
        return BlockPosition.ZERO;
    }

    @Override public Vec3D d() {
        return POSITION;
    }

    @Override public World getWorld() {
        return minecraftServer.getWorldServer(0);
    }

    @Override public @Nullable Entity f() {
        return null;
    }

    public MinecraftServer h() {
        return minecraftServer;
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

    @Override public @NotNull IChatBaseComponent getScoreboardDisplayName() {
        return new ChatComponentText(username);
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