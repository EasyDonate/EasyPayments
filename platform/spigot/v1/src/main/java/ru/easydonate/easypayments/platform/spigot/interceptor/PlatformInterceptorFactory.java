package ru.easydonate.easypayments.platform.spigot.interceptor;

import net.minecraft.server.v1_8_R1.MinecraftServer;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_8_R1.CraftServer;
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
        return new InterceptedProxiedSender(new InterceptedCommandListener(minecraftServer, permissionLevel, executorName));
    }

}
