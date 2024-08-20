package ru.easydonate.easypayments.core.platform.scheduler.bukkit;

import org.bukkit.Server;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;
import ru.easydonate.easypayments.core.platform.scheduler.PlatformScheduler;
import ru.easydonate.easypayments.core.platform.scheduler.PlatformTask;

public final class BukkitPlatformScheduler implements PlatformScheduler {

    private final BukkitScheduler bukkitScheduler;

    public BukkitPlatformScheduler(Server server) {
        this.bukkitScheduler = server.getScheduler();
    }

    @Override
    public PlatformTask runAsyncNow(Plugin plugin, Runnable task) {
        return new BukkitPlatformTask(bukkitScheduler.runTaskAsynchronously(plugin, task));
    }

    @Override
    public PlatformTask runAsyncDelayed(Plugin plugin, Runnable task, long delay) {
        return new BukkitPlatformTask(bukkitScheduler.runTaskLaterAsynchronously(plugin, task, delay));
    }

    @Override
    public PlatformTask runAsyncAtFixedRate(Plugin plugin, Runnable task, long delay, long period) {
        return new BukkitPlatformTask(bukkitScheduler.runTaskTimerAsynchronously(plugin, task, delay, period));
    }

    @Override
    public PlatformTask runSyncNow(Plugin plugin, Runnable task) {
        return new BukkitPlatformTask(bukkitScheduler.runTask(plugin, task));
    }

    @Override
    public PlatformTask runSyncDelayed(Plugin plugin, Runnable task, long delay) {
        return new BukkitPlatformTask(bukkitScheduler.runTaskLater(plugin, task, delay));
    }

    @Override
    public PlatformTask runSyncAtFixedRate(Plugin plugin, Runnable task, long delay, long period) {
        return new BukkitPlatformTask(bukkitScheduler.runTaskTimer(plugin, task, delay, period));
    }

}
