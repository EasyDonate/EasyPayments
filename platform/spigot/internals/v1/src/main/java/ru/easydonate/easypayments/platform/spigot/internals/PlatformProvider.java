package ru.easydonate.easypayments.platform.spigot.internals;

import com.mojang.authlib.GameProfile;
import net.minecraft.server.v1_8_R1.DedicatedServer;
import org.bukkit.craftbukkit.v1_8_R1.CraftServer;
import org.bukkit.craftbukkit.v1_8_R1.scheduler.CraftTask;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spigotmc.SpigotConfig;
import ru.easydonate.easypayments.core.EasyPayments;
import ru.easydonate.easypayments.core.interceptor.InterceptorFactory;
import ru.easydonate.easypayments.core.platform.provider.kind.SpigotPlatformProviderBase;
import ru.easydonate.easypayments.core.platform.scheduler.PlatformScheduler;
import ru.easydonate.easypayments.core.util.Reflection;
import ru.easydonate.easypayments.platform.spigot.internals.interceptor.PlatformInterceptorFactory;

import java.lang.reflect.Method;
import java.util.Optional;
import java.util.UUID;

public final class PlatformProvider extends SpigotPlatformProviderBase {

    private static final @Nullable Method CraftTask$getPeriod = Reflection.getDeclaredMethod(CraftTask.class, "getPeriod");

    public PlatformProvider(
            @NotNull EasyPayments plugin,
            @NotNull PlatformScheduler scheduler,
            @NotNull String executorName
    ) {
        super(plugin, scheduler, executorName);
    }

    @Override protected @NotNull InterceptorFactory createInterceptorFactory() {
        return new PlatformInterceptorFactory(this, executorName);
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

    @Override public boolean isTaskCancelled(@NotNull BukkitTask bukkitTask) {
        try {
            return super.isTaskCancelled(bukkitTask);
        } catch (Throwable ignored) {
            if (bukkitTask instanceof CraftTask) {
                Optional<Long> result = Reflection.invokeMethod(CraftTask$getPeriod, bukkitTask);
                return !result.isPresent() || result.get() == -2L;
            }

            throw new IllegalArgumentException(String.format("this bukkit task isn't a CraftTask instance! (%s)", bukkitTask.getClass()));
        }
    }

}
