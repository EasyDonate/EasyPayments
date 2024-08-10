package ru.easydonate.easypayments.task;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import ru.easydonate.easypayments.EasyPaymentsPlugin;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class AbstractPluginTask implements PluginTask, Runnable {

    protected static final Lock DATABASE_QUERIES_LOCK = new ReentrantLock();

    protected final EasyPaymentsPlugin plugin;
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
        return bukkitTask != null && !plugin.getPlatformProvider().isTaskCancelled(bukkitTask);
    }

    @Override
    public void start() {
        BukkitScheduler scheduler = plugin.getServer().getScheduler();
        long period = getPeriod();

        this.bukkitTask = period >= 0L
                ? scheduler.runTaskTimerAsynchronously(plugin, this, delay, period)
                : scheduler.runTaskLaterAsynchronously(plugin, this, delay);
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

    protected synchronized void updateActivityState() {
        this.active = isWorking();
    }

    protected void warning(String message) {
        plugin.getLogger().warning(message);
    }

    protected void warning(String format, Object... args) {
        warning(String.format(format, args));
    }

    protected void error(String message) {
        plugin.getLogger().severe(message);
    }

    protected void error(String format, Object... args) {
        error(String.format(format, args));
    }

}
