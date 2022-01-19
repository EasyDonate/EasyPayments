package ru.easydonate.easypayments.task;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class AbstractPluginTask implements PluginTask, Runnable {

    protected final Plugin plugin;
    protected final long delay;
    protected BukkitTask bukkitTask;

    protected abstract long getPeriod();

    @Override
    public boolean isWorking() {
        return bukkitTask != null;
    }

    @Override
    public void start() {
        this.bukkitTask = plugin.getServer()
                .getScheduler()
                .runTaskTimerAsynchronously(plugin, this, delay, getPeriod());
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
        this.bukkitTask = null;
    }

}
