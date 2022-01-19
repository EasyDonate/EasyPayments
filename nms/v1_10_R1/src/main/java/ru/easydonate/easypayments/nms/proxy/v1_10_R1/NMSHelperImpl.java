package ru.easydonate.easypayments.nms.proxy.v1_10_R1;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.server.v1_10_R1.*;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_10_R1.CraftServer;
import org.bukkit.craftbukkit.v1_10_R1.command.ProxiedNativeCommandSender;
import org.bukkit.craftbukkit.v1_10_R1.command.ServerCommandSender;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.Permission;
import org.jetbrains.annotations.NotNull;
import ru.easydonate.easypayments.gui.item.wrapper.NotchianItemWrapper;
import ru.easydonate.easypayments.nms.NMSHelper;
import ru.easydonate.easypayments.execution.FeedbackInterceptor;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@AllArgsConstructor
public final class NMSHelperImpl implements NMSHelper {

    private final String username;
    private final int permissionLevel;

    @Override
    public @NotNull FeedbackInterceptor createFeedbackInterceptor() {
        MinecraftServer minecraftServer = ((CraftServer) Bukkit.getServer()).getServer();
        InterceptedCommandListener commandListener = new InterceptedCommandListener(minecraftServer, permissionLevel, username);
        return new InterceptedProxiedSender(commandListener, commandListener);
    }

    @Override
    public @NotNull NotchianItemWrapper createNotchianItemWrapper(@NotNull ItemStack bukkitItem) {
        return new NMSItemWrapper(bukkitItem);
    }

    @Getter
    private static final class InterceptedCommandListener extends ServerCommandSender implements ICommandListener, FeedbackInterceptor {
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

        @Override public boolean isOp() { return true; }
        @Override public void setOp(boolean value) {}

        @Override public boolean hasPermission(String name) { return true; }
        @Override public boolean hasPermission(Permission perm) { return true; }

        @Override public boolean isPermissionSet(String name) { return true; }
        @Override public boolean isPermissionSet(Permission perm) { return true; }
    }

    private static final class InterceptedProxiedSender extends ProxiedNativeCommandSender implements FeedbackInterceptor {
        public InterceptedProxiedSender(InterceptedCommandListener orig, CommandSender sender) {
            super(orig, sender, sender);
        }

        @Override
        public InterceptedCommandListener getHandle() {
            return (InterceptedCommandListener) super.getHandle();
        }

        @Override
        public List<String> getFeedbackMessages() {
            return getHandle().getFeedbackMessages();
        }
    }

}
