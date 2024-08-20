package ru.easydonate.easypayments.task;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import ru.easydonate.easypayments.EasyPaymentsPlugin;
import ru.easydonate.easypayments.core.platform.scheduler.PlatformScheduler;
import ru.easydonate.easypayments.core.platform.scheduler.PlatformTask;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class AbstractPluginTask implements PluginTask, Runnable {

    protected static final Lock DATABASE_QUERIES_LOCK = new ReentrantLock();

    protected final EasyPaymentsPlugin plugin;
    protected final long delay;
    protected PlatformTask asyncTask;
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
        return asyncTask != null && !asyncTask.isCancelled(plugin.getPlatformProvider());
    }

    @Override
    public void start() {
        PlatformScheduler asyncScheduler = plugin.getPlatformProvider().getScheduler();
        long period = getPeriod();

        this.asyncTask = period >= 0L
                ? asyncScheduler.runAsyncAtFixedRate(plugin, this, delay, period)
                : asyncScheduler.runAsyncDelayed(plugin, this, delay);
    }

    @Override
    public void restart() {
        shutdown();
        start();
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public void shutdown() {
        if (asyncTask == null)
            return;

        this.asyncTask.cancel();
        while (isWorking() && isActive()) {}
        this.asyncTask = null;
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
