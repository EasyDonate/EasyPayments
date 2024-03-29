package ru.easydonate.easypayments.nms.provider.v1_17_R1.interceptor;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.WorldServer;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_17_R1.CraftServer;
import org.jetbrains.annotations.NotNull;
import ru.easydonate.easypayments.execution.interceptor.AbstractInterceptorFactory;
import ru.easydonate.easypayments.execution.interceptor.FeedbackInterceptor;
import ru.easydonate.easypayments.nms.provider.AbstractVersionedFeaturesProvider;

public final class VersionedInterceptorFactory extends AbstractInterceptorFactory {

    public VersionedInterceptorFactory(
            @NotNull AbstractVersionedFeaturesProvider provider,
            @NotNull String executorName,
            int permissionLevel
    ) {
        super(provider, executorName, permissionLevel);
    }

    @Override
    public @NotNull FeedbackInterceptor createFeedbackInterceptor() {
        MinecraftServer minecraftServer = ((CraftServer) Bukkit.getServer()).getServer();
        WorldServer worldServer = minecraftServer.getWorldServer(WorldServer.f);

        InterceptedCommandListener commandListener = new InterceptedCommandListener(executorName);
        InterceptedCommandListenerWrapper listenerWrapper = new InterceptedCommandListenerWrapper(
                commandListener,
                worldServer,
                executorName,
                permissionLevel
        );

        return new InterceptedProxiedSender(listenerWrapper, commandListener);
    }

}
