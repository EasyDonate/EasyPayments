package ru.easydonate.easypayments.platform.spigot;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.v1_13_R1.DedicatedServer;
import org.bukkit.craftbukkit.v1_13_R1.CraftServer;
import org.jetbrains.annotations.NotNull;
import org.spigotmc.SpigotConfig;
import ru.easydonate.easypayments.core.EasyPayments;
import ru.easydonate.easypayments.core.interceptor.InterceptorFactory;
import ru.easydonate.easypayments.core.platform.provider.kind.SpigotPlatformProviderBase;
import ru.easydonate.easypayments.core.platform.scheduler.PlatformScheduler;
import ru.easydonate.easypayments.platform.spigot.interceptor.PlatformInterceptorFactory;

import java.util.UUID;

public final class PlatformProvider extends SpigotPlatformProviderBase {

    public PlatformProvider(
            @NotNull EasyPayments plugin,
            @NotNull PlatformScheduler scheduler,
            @NotNull String executorName,
            int permissionLevel
    ) {
        super(plugin, scheduler, executorName, permissionLevel);
    }

    @Override protected @NotNull InterceptorFactory createInterceptorFactory() {
        return new PlatformInterceptorFactory(this, executorName, permissionLevel);
    }

    @Override protected @NotNull UUID resolveOfflinePlayerId(@NotNull String name) {
        // references CraftServer#getOfflinePlayer(String)
        DedicatedServer server = ((CraftServer) plugin.getServer()).getHandle().getServer();
        if (server.getOnlineMode() || SpigotConfig.bungee) {
            GameProfile profile = server.getUserCache().getProfile(name);
            if (profile != null) {
                return profile.getId();
            }
        }

        return generateOfflinePlayerId(name);
    }

}
