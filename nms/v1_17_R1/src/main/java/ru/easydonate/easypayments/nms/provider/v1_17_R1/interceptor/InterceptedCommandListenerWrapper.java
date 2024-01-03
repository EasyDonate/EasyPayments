package ru.easydonate.easypayments.nms.provider.v1_17_R1.interceptor;

import lombok.Getter;
import net.minecraft.commands.CommandListenerWrapper;
import net.minecraft.network.chat.ChatComponentText;
import net.minecraft.server.level.WorldServer;
import net.minecraft.world.phys.Vec2F;
import net.minecraft.world.phys.Vec3D;
import ru.easydonate.easypayments.execution.interceptor.FeedbackInterceptor;

import java.util.List;

@Getter
public final class InterceptedCommandListenerWrapper extends CommandListenerWrapper implements FeedbackInterceptor {

    private static final Vec3D POSITION = new Vec3D(0D, 0D, 0D);
    private static final Vec2F DIRECTION = new Vec2F(0F, 0F);

    private final InterceptedCommandListener icommandListener;

    public InterceptedCommandListenerWrapper(InterceptedCommandListener icommandListener, WorldServer worldServer, String username, int permissionLevel) {
        super(icommandListener, POSITION, DIRECTION, worldServer, permissionLevel, username, new ChatComponentText(username), worldServer.getMinecraftServer(), null);
        this.icommandListener = icommandListener;
    }

    @Override
    public List<String> getFeedbackMessages() {
        return icommandListener.getFeedbackMessages();
    }

}
