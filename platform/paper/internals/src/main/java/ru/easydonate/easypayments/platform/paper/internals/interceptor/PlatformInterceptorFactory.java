package ru.easydonate.easypayments.platform.paper.internals.interceptor;

import net.minecraft.server.level.ServerLevel;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.CraftServer;
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
        ServerLevel serverLevel = ((CraftServer) Bukkit.getServer()).getServer().overworld();
        InterceptedCommandSource commandSource = new InterceptedCommandSource(executorName);
        return new InterceptedProxiedSender(
                new InterceptedCommandSourceStack(commandSource, serverLevel, executorName, permissionLevel),
                commandSource
        );
    }

}
