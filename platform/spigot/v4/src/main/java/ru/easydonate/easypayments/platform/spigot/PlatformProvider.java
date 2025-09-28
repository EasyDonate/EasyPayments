package ru.easydonate.easypayments.platform.spigot;

import com.mojang.authlib.GameProfile;
import org.bukkit.OfflinePlayer;
import org.bukkit.craftbukkit.v1_19_R1.CraftServer;
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
    protected @NotNull OfflinePlayer createOfflinePlayer(@NotNull String name) {
        var uuid = resolvePlayerUUID(name);
        var profile = new GameProfile(uuid, name);
        return ((CraftServer) plugin.getServer()).getOfflinePlayer(profile);
    }

    @Override
    protected @NotNull InterceptorFactory createInterceptorFactory(@NotNull String executorName, int permissionLevel) {
        return new PlatformInterceptorFactory(this, executorName, permissionLevel);
    }

    private @NotNull UUID resolvePlayerUUID(@NotNull String name) {
        var server = (CraftServer) plugin.getServer();
        if (server.getOnlineMode() || SpigotConfig.bungee) {
            var profile = server.getHandle().getServer().getProfileCache().get(name);
            if (profile.isPresent()) {
                return profile.get().getId();
            }
        }

        return createOfflineUUID(name);
    }

}
