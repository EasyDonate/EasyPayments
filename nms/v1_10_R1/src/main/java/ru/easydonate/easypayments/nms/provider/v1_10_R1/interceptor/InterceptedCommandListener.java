package ru.easydonate.easypayments.nms.provider.v1_10_R1.interceptor;

import lombok.Getter;
import net.minecraft.server.v1_10_R1.*;
import org.bukkit.craftbukkit.v1_10_R1.command.ServerCommandSender;
import org.bukkit.permissions.Permission;
import ru.easydonate.easypayments.Constants;
import ru.easydonate.easypayments.execution.interceptor.FeedbackInterceptor;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Getter
public final class InterceptedCommandListener extends ServerCommandSender implements ICommandListener, FeedbackInterceptor {
    private final MinecraftServer minecraftServer;
    private final int permissionLevel;
    private final String username;
    private final List<String> feedbackMessages;

    public InterceptedCommandListener(MinecraftServer minecraftServer, int permissionLevel, String username) {
        this.minecraftServer = minecraftServer;
        this.permissionLevel = permissionLevel;
        this.username = username;
        this.feedbackMessages = new ArrayList<>();
    }

    @Override
    public boolean getSendCommandFeedback() {
        return Constants.COMMAND_SENDER_ACCEPTS_SUCCESS;
    }

    @Override
    public void a(CommandObjectiveExecutor.EnumCommandResult enumCommandResult, int i) {
    }

    @Override
    public void sendMessage(IChatBaseComponent iChatBaseComponent) {
        feedbackMessages.add(iChatBaseComponent.toPlainText());
    }

    @Override
    public boolean a(int i, String s) {
        return i <= permissionLevel;
    }

    @Override
    public BlockPosition getChunkCoordinates() {
        return BlockPosition.ZERO;
    }

    @Override
    public Vec3D d() {
        return Vec3D.a;
    }

    @Override
    public World getWorld() {
        return minecraftServer.getWorldServer(0);
    }

    @Nullable
    @Override
    public Entity f() {
        return null;
    }

    @Nullable
    @Override
    public MinecraftServer h() {
        return minecraftServer;
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
    public IChatBaseComponent getScoreboardDisplayName() {
        return new ChatComponentText(username);
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
