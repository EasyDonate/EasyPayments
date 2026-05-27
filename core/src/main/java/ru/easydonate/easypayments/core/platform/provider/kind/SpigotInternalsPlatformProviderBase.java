package ru.easydonate.easypayments.core.platform.provider.kind;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import ru.easydonate.easypayments.core.EasyPayments;
import ru.easydonate.easypayments.core.platform.scheduler.PlatformScheduler;

@Getter
public abstract class SpigotInternalsPlatformProviderBase extends SpigotLikePlatformProviderBase {

    public SpigotInternalsPlatformProviderBase(
            @NotNull EasyPayments plugin,
            @NotNull PlatformScheduler scheduler,
            @NotNull String executorName
    ) {
        super(plugin, scheduler, executorName, false);
    }

    @Override public @NotNull ImplementationType getImplementationType() {
        return ImplementationType.INTERNALS;
    }

    @Override
    public @NotNull PlatformType getPlatformType() {
        return PlatformType.SPIGOT;
    }

}
