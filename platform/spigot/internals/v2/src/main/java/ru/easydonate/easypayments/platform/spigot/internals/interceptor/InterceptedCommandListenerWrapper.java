package ru.easydonate.easypayments.platform.spigot.internals.interceptor;

import lombok.Getter;
import net.minecraft.server.v1_13_R1.*;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import ru.easydonate.easypayments.core.interceptor.FeedbackInterceptor;

import java.util.List;

@Getter
final class InterceptedCommandListenerWrapper extends CommandListenerWrapper implements FeedbackInterceptor {

    private static final Vec3D POSITION = new Vec3D(0D, 0D, 0D);
    private static final Vec2F DIRECTION = new Vec2F(0F, 0F);

    private final InterceptedCommandListener commandListener;

    public InterceptedCommandListenerWrapper(ICommandListener commandListener, WorldServer worldServer, String username, int permissionLevel) {
        super(commandListener, POSITION, DIRECTION, worldServer, permissionLevel, username, new ChatComponentText(username), worldServer.getMinecraftServer(), null);
        this.commandListener = (InterceptedCommandListener) commandListener;
    }

    @Override
    public @NotNull CommandSender getCommandSender() {
        return commandListener.getCommandSender();
    }

    @Override
    public List<String> getFeedbackMessages() {
        return commandListener.getFeedbackMessages();
    }

}
