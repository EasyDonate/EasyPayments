package ru.easydonate.easypayments.nms.provider.v1_8_R3.interceptor;

import net.minecraft.server.v1_8_R3.MinecraftServer;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_8_R3.CraftServer;
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
        InterceptedCommandListener commandListener = new InterceptedCommandListener(minecraftServer, permissionLevel, executorName);
        return new InterceptedProxiedSender(commandListener, commandListener);
    }

}
