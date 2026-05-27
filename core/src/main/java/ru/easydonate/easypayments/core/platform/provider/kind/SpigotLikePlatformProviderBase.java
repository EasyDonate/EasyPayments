package ru.easydonate.easypayments.core.platform.provider.kind;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import ru.easydonate.easypayments.core.EasyPayments;
import ru.easydonate.easypayments.core.platform.provider.PlatformProviderBase;
import ru.easydonate.easypayments.core.platform.scheduler.PlatformScheduler;

@Getter
public abstract class SpigotLikePlatformProviderBase extends PlatformProviderBase {

    private final @NotNull String providerId;
    private final @NotNull PlatformType platformType;

    public SpigotLikePlatformProviderBase(
            @NotNull EasyPayments plugin,
            @NotNull PlatformScheduler scheduler,
            @NotNull String executorName,
            boolean runningFolia
    ) {
        super(plugin, scheduler, executorName);
        this.providerId = "spigot:" + getImplementationType().getKey();
        this.platformType = runningFolia ? PlatformType.FOLIA : PlatformType.SPIGOT;
    }

}
