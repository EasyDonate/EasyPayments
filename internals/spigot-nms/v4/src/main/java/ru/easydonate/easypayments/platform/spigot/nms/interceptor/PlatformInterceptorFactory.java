package ru.easydonate.easypayments.platform.spigot.nms.interceptor;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_19_R1.CraftServer;
import org.jetbrains.annotations.NotNull;
import ru.easydonate.easypayments.core.interceptor.FeedbackInterceptor;
import ru.easydonate.easypayments.core.interceptor.InterceptorFactoryBase;
import ru.easydonate.easypayments.core.platform.provider.PlatformProviderBase;

public final class PlatformInterceptorFactory extends InterceptorFactoryBase {

    public PlatformInterceptorFactory(
            @NotNull PlatformProviderBase provider,
            @NotNull String executorName,
            int permissionLevel
    ) {
        super(provider, executorName, permissionLevel);
    }

    @Override
    public @NotNull FeedbackInterceptor createFeedbackInterceptor() {
        MinecraftServer minecraftServer = ((CraftServer) Bukkit.getServer()).getServer();
        ServerLevel serverLevel = minecraftServer.overworld();

        InterceptedCommandSource commandListener = new InterceptedCommandSource(executorName);
        InterceptedCommandSourceStack listenerWrapper = new InterceptedCommandSourceStack(
                commandListener,
                serverLevel,
                executorName,
                permissionLevel
        );

        return new InterceptedProxiedSender(listenerWrapper, commandListener);
    }

}
