package ru.easydonate.easypayments.platform.spigot.nms.interceptor;

import lombok.Getter;
import net.minecraft.server.v1_8_R1.*;
import org.bukkit.craftbukkit.v1_8_R1.command.ServerCommandSender;
import org.bukkit.permissions.Permission;
import ru.easydonate.easypayments.core.Constants;
import ru.easydonate.easypayments.core.interceptor.FeedbackInterceptor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

final class InterceptedCommandListener extends ServerCommandSender implements ICommandListener, FeedbackInterceptor {

    private static final Vec3D POSITION = new Vec3D(0D, 0D, 0D);

    private final MinecraftServer minecraftServer;
    private final int permissionLevel;
    private final String username;

    @Getter private final List<String> feedbackMessages;

    public InterceptedCommandListener(MinecraftServer minecraftServer, int permissionLevel, String username) {
        this.minecraftServer = minecraftServer;
        this.permissionLevel = permissionLevel;
        this.username = username;
        this.feedbackMessages = new ArrayList<>();
    }

    public boolean getSendCommandFeedback() {
        return Constants.COMMAND_SENDER_ACCEPTS_SUCCESS;
    }

    public void a(EnumCommandResult enumCommandResult, int i) {
    }

    public void sendMessage(IChatBaseComponent iChatBaseComponent) {
        feedbackMessages.add(iChatBaseComponent.c());
    }

    public boolean a(int i, String s) {
        return i <= permissionLevel;
    }

    public BlockPosition getChunkCoordinates() {
        return BlockPosition.ZERO;
    }

    public Vec3D d() {
        return POSITION;
    }

    public World getWorld() {
        return minecraftServer.getWorldServer(0);
    }

    public Entity f() {
        return null;
    }

    public MinecraftServer h() {
        return minecraftServer;
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

    public IChatBaseComponent getScoreboardDisplayName() {
        return new ChatComponentText(username);
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