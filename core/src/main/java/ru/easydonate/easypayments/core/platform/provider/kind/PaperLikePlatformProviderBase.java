package ru.easydonate.easypayments.core.platform.provider.kind;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import ru.easydonate.easypayments.core.EasyPayments;
import ru.easydonate.easypayments.core.platform.provider.PlatformProviderBase;
import ru.easydonate.easypayments.core.platform.scheduler.PlatformScheduler;

@Getter
public abstract class PaperLikePlatformProviderBase extends PlatformProviderBase {

    private final @NotNull String providerId;
    private final @NotNull PlatformType platformType;

    public PaperLikePlatformProviderBase(
            @NotNull EasyPayments plugin,
            @NotNull PlatformScheduler scheduler,
            @NotNull String executorName,
            int permissionLevel,
            boolean runningFolia
    ) {
        super(plugin, scheduler, executorName, permissionLevel);
        this.providerId = "paper:" + getImplementationType().getKey();
        this.platformType = PlatformType.with(runningFolia);
    }

}
