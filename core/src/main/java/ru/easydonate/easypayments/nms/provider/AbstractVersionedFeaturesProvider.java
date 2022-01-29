package ru.easydonate.easypayments.nms.provider;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import ru.easydonate.easypayments.execution.interceptor.InterceptorFactory;

import java.util.concurrent.Executor;

@Getter
public abstract class AbstractVersionedFeaturesProvider implements VersionedFeaturesProvider {

    protected final Plugin plugin;
    protected final InterceptorFactory interceptorFactory;
    protected final Executor bukkitSyncExecutor;

    public AbstractVersionedFeaturesProvider(@NotNull Plugin plugin, @NotNull String executorName, int permissionLevel) {
        this.plugin = plugin;
        this.interceptorFactory = createInterceptorFactory(executorName, permissionLevel);
        this.bukkitSyncExecutor = task -> plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, task);
    }

    @Override
    public @NotNull String getMinecraftVersion() {
        return MINECRAFT_VERSION.getVersion();
    }

    @Override
    public @NotNull String getNMSVersion() {
        return NMS_VERSION;
    }

    protected abstract @NotNull InterceptorFactory createInterceptorFactory(@NotNull String executorName, int permissionLevel);

    @RequiredArgsConstructor
    protected static final class Builder implements VersionedFeaturesProvider.Builder {

        private final Creator creator;

        private Plugin plugin;
        private String executorName;
        private int permissionLevel;

        @Override
        public @NotNull VersionedFeaturesProvider create() {
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

        @NotNull VersionedFeaturesProvider create(@NotNull Plugin plugin, @NotNull String executorName, int permissionLevel);

    }

}
