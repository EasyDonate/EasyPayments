package ru.easydonate.easypayments.task;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class AbstractPluginTask implements PluginTask, Runnable {

    protected final Plugin plugin;
    protected final long delay;
    protected BukkitTask bukkitTask;
    protected boolean active = true;

    protected long getPeriod() {
        return -1L;
    }

    @Override
    public boolean isActive() {
        return active;
    }

    @Override
    public boolean isWorking() {
        return bukkitTask != null && !bukkitTask.isCancelled();
    }

    @Override
    public void start() {
        BukkitScheduler scheduler = plugin.getServer().getScheduler();
        long period = getPeriod();

        this.bukkitTask = period >= 0L
                ? scheduler.runTaskTimerAsynchronously(plugin, this, delay, period)
                : scheduler.runTaskLater(plugin, this, delay);
    }

    @Override
    public void restart() {
        shutdown();
        start();
    }

    @Override
    public void shutdown() {
        if(bukkitTask == null)
            return;

        this.bukkitTask.cancel();
        while(isWorking() && isActive()) {}
        this.bukkitTask = null;
    }

    @Override
    public @NotNull CompletableFuture<Void> shutdownAsync() {
        return CompletableFuture.runAsync(this::shutdown);
    }

    protected void updateActivityState() {
        if(!isWorking())
            this.active = false;
    }

    protected void warning(String format, Object... args) {
        plugin.getLogger().warning(String.format(format, args));
    }

    protected void error(String format, Object... args) {
        plugin.getLogger().severe(String.format(format, args));
    }

}
