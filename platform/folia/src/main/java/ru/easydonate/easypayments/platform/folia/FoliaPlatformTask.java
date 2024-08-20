package ru.easydonate.easypayments.platform.folia;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import ru.easydonate.easypayments.core.platform.provider.PlatformProvider;
import ru.easydonate.easypayments.core.platform.scheduler.PlatformTask;

@Getter
@AllArgsConstructor(access = AccessLevel.PACKAGE)
public class FoliaPlatformTask implements PlatformTask {

    private final ScheduledTask handle;

    @Override
    public boolean isCancelled(PlatformProvider platformProvider) {
        return handle == null || handle.isCancelled();
    }

    @Override
    public void cancel() {
        if (handle != null) {
            handle.cancel();
        }
    }

}
