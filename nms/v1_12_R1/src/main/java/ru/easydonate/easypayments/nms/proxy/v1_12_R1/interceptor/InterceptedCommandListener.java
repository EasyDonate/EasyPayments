package ru.easydonate.easypayments.nms.proxy.v1_12_R1.interceptor;

import lombok.Getter;
import net.minecraft.server.v1_12_R1.IChatBaseComponent;
import net.minecraft.server.v1_12_R1.ICommandListener;
import net.minecraft.server.v1_12_R1.MinecraftServer;
import net.minecraft.server.v1_12_R1.World;
import org.bukkit.craftbukkit.v1_12_R1.command.ServerCommandSender;
import org.bukkit.permissions.Permission;
import ru.easydonate.easypayments.execution.interceptor.FeedbackInterceptor;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Getter
final class InterceptedCommandListener extends ServerCommandSender implements ICommandListener, FeedbackInterceptor {

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
        return true;
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
    public World getWorld() {
        return minecraftServer.getWorldServer(0);
    }

    @Nullable
    @Override
    public MinecraftServer C_() {
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
