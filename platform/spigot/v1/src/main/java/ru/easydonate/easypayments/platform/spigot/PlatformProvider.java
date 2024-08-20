package ru.easydonate.easypayments.platform.spigot;

import org.bukkit.craftbukkit.v1_8_R1.scheduler.CraftTask;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import ru.easydonate.easypayments.core.interceptor.InterceptorFactory;
import ru.easydonate.easypayments.core.platform.provider.PlatformProviderBase;
import ru.easydonate.easypayments.core.platform.scheduler.PlatformScheduler;
import ru.easydonate.easypayments.core.util.Reflection;
import ru.easydonate.easypayments.platform.spigot.interceptor.PlatformInterceptorFactory;

import java.lang.reflect.Method;
import java.util.Optional;

public final class PlatformProvider extends PlatformProviderBase {

    private static final Method getPeriod = Reflection.getDeclaredMethod(CraftTask.class, "getPeriod");

    public PlatformProvider(
            @NotNull Plugin plugin,
            @NotNull PlatformScheduler scheduler,
            @NotNull String executorName,
            int permissionLevel
    ) {
        super(plugin, scheduler, executorName, permissionLevel);
    }

    @Override
    protected @NotNull InterceptorFactory createInterceptorFactory(@NotNull String executorName, int permissionLevel) {
        return new PlatformInterceptorFactory(this, executorName, permissionLevel);
    }

    @Override
    public boolean isTaskCancelled(@NotNull BukkitTask bukkitTask) {
        try {
            return super.isTaskCancelled(bukkitTask);
        } catch (Throwable ignored) {
            if (bukkitTask instanceof CraftTask) {
                Optional<Long> result = Reflection.invokeMethod(getPeriod, bukkitTask);
                return !result.isPresent() || result.get() == -2L;
            }

            throw new IllegalArgumentException("this bukkit task isn't a CraftTask instance! (" + bukkitTask.getClass() + ")");
        }
    }

}
