package ru.easydonate.easypayments.core.platform.provider;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.easydonate.easypayments.core.interceptor.InterceptorFactory;

import java.util.concurrent.Executor;

@Getter
public abstract class PlatformProviderBase implements PlatformProvider {

    protected final Plugin plugin;
    protected final Executor bukkitSyncExecutor;
    protected InterceptorFactory interceptorFactory;

    public PlatformProviderBase(@NotNull Plugin plugin, @NotNull String executorName, int permissionLevel) {
        this.plugin = plugin;
        this.interceptorFactory = createInterceptorFactory(executorName, permissionLevel);
        this.bukkitSyncExecutor = task -> plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, task);
    }

    @Override
    public @NotNull String getMinecraftVersion() {
        return MINECRAFT_VERSION.getVersion();
    }

    @Override
    public @Nullable String getNMSVersion() {
        return NMS_VERSION;
    }

    protected abstract @NotNull InterceptorFactory createInterceptorFactory(@NotNull String executorName, int permissionLevel);

    public synchronized void updateInterceptorFactory(@NotNull String executorName, int permissionLevel) {
        this.interceptorFactory = createInterceptorFactory(executorName, permissionLevel);
    }

    @Override
    public boolean isTaskCancelled(@Nullable BukkitTask bukkitTask) {
        return bukkitTask == null || bukkitTask.isCancelled();
    }

    @RequiredArgsConstructor
    public static final class Builder implements PlatformProvider.Builder {

        private final Creator creator;

        private Plugin plugin;
        private String executorName;
        private int permissionLevel;

        @Override
        public @NotNull PlatformProvider create() {
            return creator.create(plugin, executorName, permissionLevel);
        }

        @Override
        public @NotNull Builder withPlugin(@NotNull Plugin plugin) {
            this.plugin = plugin;
            return this;
        }

        @Override
        public @NotNull Builder withExecutorName(@NotNull String executorName) {
            this.executorName = executorName;
            return this;
        }

        @Override
        public @NotNull Builder withPermissionLevel(int permissionLevel) {
            this.permissionLevel = permissionLevel;
            return this;
        }

    }

    @FunctionalInterface
    protected interface Creator {

        @NotNull PlatformProvider create(@NotNull Plugin plugin, @NotNull String executorName, int permissionLevel);

    }

}
