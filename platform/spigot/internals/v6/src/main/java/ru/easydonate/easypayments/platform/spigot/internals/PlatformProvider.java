package ru.easydonate.easypayments.platform.spigot.internals;

import org.bukkit.craftbukkit.v1_21_R7.CraftServer;
import org.jetbrains.annotations.NotNull;
import org.spigotmc.SpigotConfig;
import ru.easydonate.easypayments.core.EasyPayments;
import ru.easydonate.easypayments.core.interceptor.InterceptorFactory;
import ru.easydonate.easypayments.core.platform.provider.kind.SpigotInternalsPlatformProviderBase;
import ru.easydonate.easypayments.core.platform.scheduler.PlatformScheduler;
import ru.easydonate.easypayments.platform.spigot.internals.interceptor.PlatformInterceptorFactory;

import java.util.UUID;

public final class PlatformProvider extends SpigotInternalsPlatformProviderBase {

    public PlatformProvider(
            @NotNull EasyPayments plugin,
            @NotNull PlatformScheduler scheduler,
            @NotNull String executorName,
            boolean runningFolia
    ) {
        super(plugin, scheduler, executorName, runningFolia);
    }

    @Override protected @NotNull InterceptorFactory createInterceptorFactory() {
        return new PlatformInterceptorFactory(this, executorName, runningFolia);
    }

    @Override protected @NotNull UUID resolveOfflinePlayerId(@NotNull String name) {
        // references CraftServer#getOfflinePlayer(String)
        var server = (CraftServer) plugin.getServer();
        if (server.getOnlineMode() || SpigotConfig.bungee) {
            var profile = server.getHandle().getServer().services().nameToIdCache().get(name);
            if (profile.isPresent()) {
                return profile.get().id();
            }
        }

        return generateOfflinePlayerId(name);
    }

}
