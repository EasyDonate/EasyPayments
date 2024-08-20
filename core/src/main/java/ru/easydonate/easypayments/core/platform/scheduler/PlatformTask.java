package ru.easydonate.easypayments.core.platform.scheduler;

import ru.easydonate.easypayments.core.platform.provider.PlatformProvider;

public interface PlatformTask {

    boolean isCancelled(PlatformProvider platformProvider);

    void cancel();

}
