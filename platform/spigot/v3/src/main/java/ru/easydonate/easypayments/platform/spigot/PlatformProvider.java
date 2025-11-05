package ru.easydonate.easypayments.platform.spigot;

import org.bukkit.craftbukkit.v1_17_R1.CraftServer;
import org.jetbrains.annotations.Blocking;
import org.jetbrains.annotations.NonBlocking;
import org.jetbrains.annotations.NotNull;
import org.spigotmc.SpigotConfig;
import ru.easydonate.easypayments.core.EasyPayments;
import ru.easydonate.easypayments.core.interceptor.InterceptorFactory;
import ru.easydonate.easypayments.core.platform.provider.PlatformProviderBase;
import ru.easydonate.easypayments.core.platform.scheduler.PlatformScheduler;
import ru.easydonate.easypayments.platform.spigot.interceptor.PlatformInterceptorFactory;

import java.util.UUID;

public final class PlatformProvider extends PlatformProviderBase {

    public PlatformProvider(
            @NotNull EasyPayments plugin,
            @NotNull PlatformScheduler scheduler,
            @NotNull String executorName,
            int permissionLevel
    ) {
        super(plugin, scheduler, executorName, permissionLevel);
    }

    @Override
    @NonBlocking
    protected @NotNull InterceptorFactory interceptorFactoryOf(@NotNull String executorName, int permissionLevel) {
        return new PlatformInterceptorFactory(this, executorName, permissionLevel);
    }

    @Override
    @Blocking
    protected @NotNull UUID resolveOfflinePlayerId(@NotNull String name) {
        // references CraftServer#getOfflinePlayer(String)
        var server = (CraftServer) plugin.getServer();
        if (server.getOnlineMode() || SpigotConfig.bungee) {
            var profile = server.getHandle().getServer().getProfileCache().get(name);
            if (profile.isPresent()) {
                return profile.get().getId();
            }
        }

        return generateOfflinePlayerId(name);
    }

}
