package ru.easydonate.easypayments.core.platform.provider.kind;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import ru.easydonate.easypayments.core.EasyPayments;
import ru.easydonate.easypayments.core.platform.provider.PlatformProviderBase;
import ru.easydonate.easypayments.core.platform.scheduler.PlatformScheduler;

@Getter
public abstract class SpigotPlatformProviderBase extends PlatformProviderBase {

    private static final @NotNull String PROVIDER_ID = "spigot:internals";

    public SpigotPlatformProviderBase(
            @NotNull EasyPayments plugin,
            @NotNull PlatformScheduler scheduler,
            @NotNull String executorName,
            int permissionLevel
    ) {
        super(plugin, scheduler, executorName, permissionLevel);
    }

    @Override public @NotNull String getProviderId() {
        return PROVIDER_ID;
    }

    @Override public @NotNull ImplementationType getImplementationType() {
        return ImplementationType.INTERNALS;
    }

    @Override
    public @NotNull PlatformType getPlatformType() {
        return PlatformType.SPIGOT;
    }

}
