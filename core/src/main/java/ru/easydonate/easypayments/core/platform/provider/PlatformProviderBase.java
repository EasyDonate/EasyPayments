package ru.easydonate.easypayments.core.platform.provider;

import lombok.Getter;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import ru.easydonate.easypayments.core.interceptor.InterceptorFactory;
import ru.easydonate.easypayments.core.platform.scheduler.PlatformScheduler;

import java.util.concurrent.Executor;

@Getter
public abstract class PlatformProviderBase implements PlatformProvider {

    private static final String NAME = "Spigot Internals";

    protected final Plugin plugin;
    protected final PlatformScheduler scheduler;
    protected final Executor syncExecutor;
    protected InterceptorFactory interceptorFactory;

    public PlatformProviderBase(@NotNull Plugin plugin, @NotNull PlatformScheduler scheduler, @NotNull String executorName, int permissionLevel) {
        this.plugin = plugin;
        this.scheduler = scheduler;
        this.syncExecutor = task -> scheduler.runSyncNow(plugin, task);
        this.interceptorFactory = createInterceptorFactory(executorName, permissionLevel);
    }

    @Override
    public @NotNull String getName() {
        return NAME;
    }

    protected abstract @NotNull InterceptorFactory createInterceptorFactory(@NotNull String executorName, int permissionLevel);

    public synchronized void updateInterceptorFactory(@NotNull String executorName, int permissionLevel) {
        this.interceptorFactory = createInterceptorFactory(executorName, permissionLevel);
    }

    @Override
    public boolean isTaskCancelled(@NotNull BukkitTask bukkitTask) {
        return bukkitTask.isCancelled();
    }

}
