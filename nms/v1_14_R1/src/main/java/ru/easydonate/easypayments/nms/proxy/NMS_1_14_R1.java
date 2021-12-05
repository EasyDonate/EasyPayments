package ru.easydonate.easypayments.nms.proxy;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.server.v1_14_R1.*;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_14_R1.CraftServer;
import org.bukkit.craftbukkit.v1_14_R1.command.ProxiedNativeCommandSender;
import org.bukkit.craftbukkit.v1_14_R1.command.ServerCommandSender;
import org.bukkit.permissions.Permission;
import ru.easydonate.easypayments.nms.NMSHelper;
import ru.easydonate.easypayments.execution.FeedbackInterceptor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@AllArgsConstructor
public class NMS_1_14_R1 implements NMSHelper {

    private final String username;
    private final int permissionLevel;

    @Override
    public FeedbackInterceptor createFeedbackInterceptor() {
        MinecraftServer minecraftServer = ((CraftServer) Bukkit.getServer()).getServer();
        WorldServer worldServer = minecraftServer.getWorldServer(DimensionManager.OVERWORLD);

        InterceptedCommandListener commandListener = new InterceptedCommandListener(username);
        InterceptedCommandListenerWrapper listenerWrapper = new InterceptedCommandListenerWrapper(
                commandListener,
                worldServer,
                username,
                permissionLevel
        );

        return new InterceptedProxiedSender(listenerWrapper, commandListener);
    }

    @Getter
    private static final class InterceptedCommandListenerWrapper extends CommandListenerWrapper implements FeedbackInterceptor {
        private static final Vec3D POSITION = new Vec3D(0D, 0D, 0D);
        private static final Vec2F DIRECTION = new Vec2F(0F, 0F);

        private final InterceptedCommandListener icommandlistener;

        public InterceptedCommandListenerWrapper(InterceptedCommandListener icommandlistener, WorldServer worldserver, String username, int permissionLevel) {
            super(icommandlistener, POSITION, DIRECTION, worldserver, permissionLevel, username, new ChatComponentText(username), worldserver.getMinecraftServer(), null);
            this.icommandlistener = icommandlistener;
        }

        @Override
        public List<String> getFeedbackMessages() {
            return icommandlistener.getFeedbackMessages();
        }
    }

    @Getter
    private static final class InterceptedCommandListener extends ServerCommandSender implements ICommandListener {
        private final String username;
        private final List<String> feedbackMessages;

        public InterceptedCommandListener(String username) {
            this.username = username;
            this.feedbackMessages = new ArrayList<>();
        }

        @Override
        public void sendMessage(IChatBaseComponent iChatBaseComponent) {
            feedbackMessages.add(iChatBaseComponent.getString());
        }

        @Override
        public boolean shouldSendSuccess() {
            return true;
        }

        @Override
        public boolean shouldSendFailure() {
            return true;
        }

        @Override
        public boolean shouldBroadcastCommands() {
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

        @Override public boolean isOp() { return true; }
        @Override public void setOp(boolean value) {}

        @Override public boolean hasPermission(String name) { return true; }
        @Override public boolean hasPermission(Permission perm) { return true; }

        @Override public boolean isPermissionSet(String name) { return true; }
        @Override public boolean isPermissionSet(Permission perm) { return true; }
    }

    private static final class InterceptedProxiedSender extends ProxiedNativeCommandSender implements FeedbackInterceptor {
        public InterceptedProxiedSender(InterceptedCommandListenerWrapper orig, CommandSender sender) {
            super(orig, sender, sender);
        }

        @Override
        public InterceptedCommandListenerWrapper getHandle() {
            return (InterceptedCommandListenerWrapper) super.getHandle();
        }

        @Override
        public List<String> getFeedbackMessages() {
            return getHandle().getFeedbackMessages();
        }
    }

}
