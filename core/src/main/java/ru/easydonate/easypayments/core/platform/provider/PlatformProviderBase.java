package ru.easydonate.easypayments.core.platform.provider;

import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import lombok.AccessLevel;
import lombok.Getter;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.Blocking;
import org.jetbrains.annotations.NonBlocking;
import org.jetbrains.annotations.NotNull;
import ru.easydonate.easypayments.core.EasyPayments;
import ru.easydonate.easypayments.core.interceptor.InterceptorFactory;
import ru.easydonate.easypayments.core.platform.scheduler.PlatformScheduler;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Executor;

@Getter
public abstract class PlatformProviderBase implements PlatformProvider {

    private static final @NotNull String NAME = "Spigot Internals";

    protected final @NotNull EasyPayments plugin;
    protected final @NotNull PlatformScheduler scheduler;
    protected final @NotNull Executor syncExecutor;

    @Getter(AccessLevel.NONE) protected @NotNull String executorName;
    @Getter(AccessLevel.NONE) protected int permissionLevel;
    protected @NotNull InterceptorFactory interceptorFactory;

    public PlatformProviderBase(
            @NotNull EasyPayments plugin,
            @NotNull PlatformScheduler scheduler,
            @NotNull String executorName,
            int permissionLevel
    ) {
        this.plugin = plugin;
        this.scheduler = scheduler;
        this.syncExecutor = task -> scheduler.runSyncNow(plugin, task);

        this.executorName = executorName;
        this.permissionLevel = permissionLevel;
        this.interceptorFactory = createInterceptorFactory();
    }

    @NonBlocking
    protected abstract @NotNull InterceptorFactory createInterceptorFactory();

    @Blocking
    protected abstract @NotNull UUID resolveOfflinePlayerId(@NotNull String name);

    @Override
    public @NotNull String getName() {
        return NAME;
    }

    @Override
    @Blocking
    public @NotNull UUID resolvePlayerId(@NotNull String name) {
        Preconditions.checkArgument(name != null, "name cannot be null");
        Preconditions.checkArgument(!name.isEmpty(), "name cannot be empty");

        return findOnlinePlayer(name)
                .map(Player::getUniqueId)
                .orElseGet(() -> findOfflinePlayer(name)
                        .map(OfflinePlayer::getUniqueId)
                        .orElseGet(() -> resolveOfflinePlayerId(name)));
    }

    @Override
    public boolean isTaskCancelled(@NotNull BukkitTask bukkitTask) {
        return bukkitTask.isCancelled();
    }

    public final void updateInterceptorFactory(@NotNull PlatformResolverState state) {
        updateInterceptorFactory(state.getExecutorName(), state.getPermissionLevel());
    }

    public final synchronized void updateInterceptorFactory(@NotNull String executorName, int permissionLevel) {
        if (Objects.equals(this.executorName, executorName) && this.permissionLevel == permissionLevel)
            return;

        this.executorName = executorName;
        this.permissionLevel = permissionLevel;
        this.interceptorFactory = createInterceptorFactory();
    }

    protected final @NotNull UUID generateOfflinePlayerId(@NotNull String name) {
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
