package ru.easydonate.easypayments.platform.folia;

import io.papermc.paper.threadedregions.scheduler.AsyncScheduler;
import io.papermc.paper.threadedregions.scheduler.GlobalRegionScheduler;
import org.bukkit.Server;
import org.bukkit.plugin.Plugin;
import ru.easydonate.easypayments.core.platform.scheduler.PlatformScheduler;
import ru.easydonate.easypayments.core.platform.scheduler.PlatformTask;

import java.util.concurrent.TimeUnit;

public final class FoliaPlatformScheduler implements PlatformScheduler {

    private final AsyncScheduler asyncScheduler;
    private final GlobalRegionScheduler globalScheduler;

    public FoliaPlatformScheduler(Server server) {
        this.asyncScheduler = server.getAsyncScheduler();
        this.globalScheduler = server.getGlobalRegionScheduler();
    }

    @Override
    public PlatformTask runAsyncNow(Plugin plugin, Runnable task) {
        return new FoliaPlatformTask(asyncScheduler.runNow(plugin, handle -> task.run()));
    }

    @Override
    public PlatformTask runAsyncDelayed(Plugin plugin, Runnable task, long delay) {
        return new FoliaPlatformTask(asyncScheduler.runDelayed(plugin, handle -> task.run(), delay * 50L, TimeUnit.MILLISECONDS));
    }

    @Override
    public PlatformTask runAsyncAtFixedRate(Plugin plugin, Runnable task, long delay, long period) {
        return new FoliaPlatformTask(asyncScheduler.runAtFixedRate(plugin, handle -> task.run(), delay * 50L, period * 50L, TimeUnit.MILLISECONDS));
    }

    @Override
    public PlatformTask runSyncNow(Plugin plugin, Runnable task) {
        return new FoliaPlatformTask(globalScheduler.run(plugin, handle -> task.run()));
    }

    @Override
    public PlatformTask runSyncDelayed(Plugin plugin, Runnable task, long delay) {
        return new FoliaPlatformTask(globalScheduler.runDelayed(plugin, handle -> task.run(), delay));
    }

    @Override
    public PlatformTask runSyncAtFixedRate(Plugin plugin, Runnable task, long delay, long period) {
        return new FoliaPlatformTask(globalScheduler.runAtFixedRate(plugin, handle -> task.run(), delay, period));
    }

}
