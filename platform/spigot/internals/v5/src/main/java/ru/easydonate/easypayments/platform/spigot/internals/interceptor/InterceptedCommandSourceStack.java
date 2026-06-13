package ru.easydonate.easypayments.platform.spigot.internals.interceptor;

import lombok.Getter;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import ru.easydonate.easypayments.core.interceptor.FeedbackInterceptor;

import java.util.List;

import static ru.easydonate.easypayments.core.Constants.DEFAULT_PERMISSION_LEVEL;

@Getter
final class InterceptedCommandSourceStack extends CommandSourceStack implements FeedbackInterceptor {

    private static final @NotNull Vec3 POSITION = new Vec3(0D, 0D, 0D);
    private static final @NotNull Vec2 DIRECTION = new Vec2(0F, 0F);

    private final @NotNull InterceptedRconCommandSource commandSource;

    public InterceptedCommandSourceStack(
            @NotNull InterceptedRconCommandSource commandSource,
            @NotNull ServerLevel serverLevel,
            @NotNull String username
    ) {
        super(
                commandSource,
                POSITION, DIRECTION, serverLevel,
                DEFAULT_PERMISSION_LEVEL, username, Component.literal(username),
                serverLevel.getServer(), null
        );

        this.commandSource = commandSource;
    }

    @Override public @NotNull CommandSender getCommandSender() {
        return commandSource.getCommandSender();
    }

    @Override public @NotNull List<String> getFeedbackMessages() {
        return commandSource.getFeedbackMessages();
    }

}
