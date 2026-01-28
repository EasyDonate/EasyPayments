package ru.easydonate.easypayments.platform.paper.internals.v2;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import ru.easydonate.easypayments.core.EasyPayments;
import ru.easydonate.easypayments.core.interceptor.InterceptorFactory;
import ru.easydonate.easypayments.core.platform.provider.kind.PaperLikePlatformProviderBase;
import ru.easydonate.easypayments.core.platform.scheduler.PlatformScheduler;
import ru.easydonate.easypayments.platform.paper.internals.v2.interceptor.PlatformInterceptorFactory;

import java.util.UUID;

@Getter
public final class PlatformProvider extends PaperLikePlatformProviderBase {

    public PlatformProvider(
            @NotNull EasyPayments plugin,
            @NotNull PlatformScheduler scheduler,
            @NotNull String executorName,
            boolean runningFolia
    ) {
        super(plugin, scheduler, executorName, runningFolia);
    }

    @Override public @NotNull ImplementationType getImplementationType() {
        return ImplementationType.INTERNALS;
    }

    @Override protected @NotNull InterceptorFactory createInterceptorFactory() {
        return new PlatformInterceptorFactory(this, executorName);
    }

    @Override protected @NotNull UUID resolveOfflinePlayerId(@NotNull String name) {
        var uuid = plugin.getServer().getPlayerUniqueId(name);
        return uuid != null ? uuid : generateOfflinePlayerId(name);
    }

}
