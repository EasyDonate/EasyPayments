package ru.easydonate.easypayments.nms.provider.v1_20_R3.interceptor;

import lombok.Getter;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.PlainTextContents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import ru.easydonate.easypayments.execution.interceptor.FeedbackInterceptor;

import java.util.List;

@Getter
final class InterceptedCommandListenerWrapper extends CommandSourceStack implements FeedbackInterceptor {

    private static final Vec3 POSITION = new Vec3(0D, 0D, 0D);
    private static final Vec2 DIRECTION = new Vec2(0F, 0F);

    private final InterceptedCommandListener commandListener;

    public InterceptedCommandListenerWrapper(InterceptedCommandListener commandListener, ServerLevel serverLevel, String username, int permissionLevel) {
        super(commandListener, POSITION, DIRECTION, serverLevel, permissionLevel, username, MutableComponent.create(PlainTextContents.create(username)), serverLevel.getServer(), null);
        this.commandListener = commandListener;
    }

    @Override
    public @NotNull List<String> getFeedbackMessages() {
        return commandListener.getFeedbackMessages();
    }

}
