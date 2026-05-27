package ru.easydonate.easypayments.platform.spigot.unrelocated.v1;

import lombok.Getter;
import org.bukkit.craftbukkit.CraftServer;
import org.jetbrains.annotations.NotNull;
import org.spigotmc.SpigotConfig;
import ru.easydonate.easypayments.core.EasyPayments;
import ru.easydonate.easypayments.core.interceptor.InterceptorFactory;
import ru.easydonate.easypayments.core.platform.provider.kind.SpigotLikePlatformProviderBase;
import ru.easydonate.easypayments.core.platform.scheduler.PlatformScheduler;
import ru.easydonate.easypayments.platform.spigot.unrelocated.v1.interceptor.PlatformInterceptorFactory;

import java.util.UUID;

@Getter
public final class PlatformProvider extends SpigotLikePlatformProviderBase {

    public PlatformProvider(
            @NotNull EasyPayments plugin,
            @NotNull PlatformScheduler scheduler,
            @NotNull String executorName,
            boolean runningFolia
    ) {
        super(plugin, scheduler, executorName, runningFolia);
    }

    @Override public @NotNull ImplementationType getImplementationType() {
        return ImplementationType.UNRELOCATED;
    }

    @Override protected @NotNull InterceptorFactory createInterceptorFactory() {
        return new PlatformInterceptorFactory(this, executorName);
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
