package ru.easydonate.easypayments.core.platform.provider;

import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import lombok.Getter;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.Blocking;
import org.jetbrains.annotations.NotNull;
import ru.easydonate.easypayments.core.EasyPayments;
import ru.easydonate.easypayments.core.interceptor.InterceptorFactory;
import ru.easydonate.easypayments.core.platform.scheduler.PlatformScheduler;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Executor;

@Getter
public abstract class PlatformProviderBase implements PlatformProvider {

    private static final String NAME = "Spigot Internals";

    protected final EasyPayments plugin;
    protected final PlatformScheduler scheduler;
    protected final Executor syncExecutor;
    protected InterceptorFactory interceptorFactory;

    public PlatformProviderBase(@NotNull EasyPayments plugin, @NotNull PlatformScheduler scheduler, @NotNull String executorName, int permissionLevel) {
        this.plugin = plugin;
        this.scheduler = scheduler;
        this.syncExecutor = task -> scheduler.runSyncNow(plugin, task);
        this.interceptorFactory = createInterceptorFactory(executorName, permissionLevel);
    }

    @Override
    public @NotNull String getName() {
        return NAME;
    }

    @Override
    public final @NotNull OfflinePlayer getOfflinePlayer(@NotNull String name) {
        Preconditions.checkArgument(name != null, "name cannot be null");
        Preconditions.checkArgument(!name.isEmpty(), "name cannot be empty");

        Optional<Player> onlinePlayer = findOnlinePlayer(name);
        if (onlinePlayer.isPresent())
            return onlinePlayer.get();

        return findOfflinePlayer(name).orElseGet(() -> createOfflinePlayer(name));
    }

    @Blocking
    protected abstract @NotNull OfflinePlayer createOfflinePlayer(@NotNull String name);

    protected abstract @NotNull InterceptorFactory createInterceptorFactory(@NotNull String executorName, int permissionLevel);

    public synchronized void updateInterceptorFactory(@NotNull String executorName, int permissionLevel) {
        this.interceptorFactory = createInterceptorFactory(executorName, permissionLevel);
    }

    @Override
    public boolean isTaskCancelled(@NotNull BukkitTask bukkitTask) {
        return bukkitTask.isCancelled();
    }

    protected final @NotNull UUID createOfflineUUID(@NotNull String name) {
        return UUID.nameUUIDFromBytes(("OfflinePlayer:" + name).getBytes(Charsets.UTF_8));
    }

    private @NotNull Optional<Player> findOnlinePlayer(@NotNull String name) {
        for (Player candidate : plugin.getServer().getOnlinePlayers())
            if (name.equals(candidate.getName()))
                return Optional.of(candidate);

        return Optional.empty();
    }

    private @NotNull Optional<OfflinePlayer> findOfflinePlayer(@NotNull String name) {
        for (OfflinePlayer candidate : plugin.getServer().getOfflinePlayers())
            if (name.equals(candidate.getName()))
                return Optional.of(candidate);

        return Optional.empty();
    }

}
