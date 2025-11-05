package ru.easydonate.easypayments.core.platform.provider;

import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.Blocking;
import org.jetbrains.annotations.NonBlocking;
import org.jetbrains.annotations.NotNull;
import ru.easydonate.easypayments.core.EasyPayments;
import ru.easydonate.easypayments.core.interceptor.InterceptorFactory;
import ru.easydonate.easypayments.core.platform.scheduler.PlatformScheduler;

import java.util.UUID;

public interface PlatformProvider {

    @NotNull EasyPayments getPlugin();

    @NotNull PlatformScheduler getScheduler();

    @NotNull String getName();

    @NonBlocking
    @NotNull InterceptorFactory getInterceptorFactory();

    @Blocking
    @NotNull UUID resolvePlayerId(@NotNull String name);

    boolean isTaskCancelled(@NotNull BukkitTask asyncTask);

}
