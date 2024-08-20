package ru.easydonate.easypayments.core.platform.provider;

import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import ru.easydonate.easypayments.core.interceptor.InterceptorFactory;
import ru.easydonate.easypayments.core.platform.scheduler.PlatformScheduler;

public interface PlatformProvider {

    @NotNull Plugin getPlugin();

    @NotNull PlatformScheduler getScheduler();

    @NotNull String getName();

    @NotNull InterceptorFactory getInterceptorFactory();

    boolean isTaskCancelled(@NotNull BukkitTask asyncTask);

}
