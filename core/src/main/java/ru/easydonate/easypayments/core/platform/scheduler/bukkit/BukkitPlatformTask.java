package ru.easydonate.easypayments.core.platform.scheduler.bukkit;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.scheduler.BukkitTask;
import ru.easydonate.easypayments.core.platform.provider.PlatformProvider;
import ru.easydonate.easypayments.core.platform.scheduler.PlatformTask;

@Getter
@AllArgsConstructor(access = AccessLevel.PACKAGE)
public final class BukkitPlatformTask implements PlatformTask {

    private final BukkitTask handle;

    @Override
    public boolean isCancelled(PlatformProvider platformProvider) {
        return handle == null || platformProvider.isTaskCancelled(handle);
    }

    @Override
    public void cancel() {
        if (handle != null) {
            handle.cancel();
        }
    }

}
