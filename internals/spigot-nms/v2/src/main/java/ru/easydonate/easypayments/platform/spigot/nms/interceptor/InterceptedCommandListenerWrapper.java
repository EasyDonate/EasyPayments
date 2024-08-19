package ru.easydonate.easypayments.platform.spigot.nms.interceptor;

import lombok.Getter;
import net.minecraft.server.v1_13_R1.*;
import ru.easydonate.easypayments.core.interceptor.FeedbackInterceptor;

import java.util.List;

@Getter
public final class InterceptedCommandListenerWrapper extends CommandListenerWrapper implements FeedbackInterceptor {

    private static final Vec3D POSITION = new Vec3D(0D, 0D, 0D);
    private static final Vec2F DIRECTION = new Vec2F(0F, 0F);

    private final InterceptedCommandListener icommandlistener;

    public InterceptedCommandListenerWrapper(ICommandListener icommandlistener, WorldServer worldserver, String username, int permissionLevel) {
        super(icommandlistener, POSITION, DIRECTION, worldserver, permissionLevel, username, new ChatComponentText(username), worldserver.getMinecraftServer(), null);
        this.icommandlistener = (InterceptedCommandListener) icommandlistener;
    }

    @Override
    public List<String> getFeedbackMessages() {
        return icommandlistener.getFeedbackMessages();
    }

}
