package ru.easydonate.easypayments.core.platform.scheduler;

import org.bukkit.Server;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import ru.easydonate.easypayments.core.platform.scheduler.bukkit.BukkitPlatformScheduler;

public interface PlatformScheduler {

    @NotNull String FOLIA_PLATFORM_SCHEDULER = "ru.easydonate.easypayments.platform.folia.FoliaPlatformScheduler";

    static PlatformScheduler create(Server server, boolean runningFolia) {
        if (runningFolia) {
            try {
                Class<?> clazz = Class.forName(FOLIA_PLATFORM_SCHEDULER);
                return (PlatformScheduler) clazz.getConstructor(Server.class).newInstance(server);
            } catch (Exception ex) {
                throw new RuntimeException("Couldn't instantiate Folia platform scheduler!");
            }
        }

        return new BukkitPlatformScheduler(server);
    }

    PlatformTask runAsyncNow(Plugin plugin, Runnable task);

    PlatformTask runAsyncDelayed(Plugin plugin, Runnable task, long delay);

    PlatformTask runAsyncAtFixedRate(Plugin plugin, Runnable task, long delay, long period);

    PlatformTask runSyncNow(Plugin plugin, Runnable task);

    PlatformTask runSyncDelayed(Plugin plugin, Runnable task, long delay);

    PlatformTask runSyncAtFixedRate(Plugin plugin, Runnable task, long delay, long period);

}
