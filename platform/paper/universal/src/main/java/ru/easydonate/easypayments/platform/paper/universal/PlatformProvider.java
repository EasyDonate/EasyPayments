package ru.easydonate.easypayments.platform.paper.universal;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import ru.easydonate.easypayments.core.EasyPayments;
import ru.easydonate.easypayments.core.interceptor.InterceptorFactory;
import ru.easydonate.easypayments.core.platform.provider.kind.PaperLikePlatformProviderBase;
import ru.easydonate.easypayments.core.platform.scheduler.PlatformScheduler;
import ru.easydonate.easypayments.platform.paper.universal.interceptor.PlatformInterceptorFactory;

import java.util.UUID;

@Getter
public final class PlatformProvider extends PaperLikePlatformProviderBase {

    public PlatformProvider(
            @NotNull EasyPayments plugin,
            @NotNull PlatformScheduler scheduler,
            @NotNull String executorName,
            int permissionLevel,
            boolean runningFolia
    ) {
        super(plugin, scheduler, executorName, permissionLevel, runningFolia);
    }

    @Override public @NotNull ImplementationType getImplementationType() {
        return ImplementationType.UNIVERSAL;
    }

    @Override protected @NotNull InterceptorFactory createInterceptorFactory() {
        return new PlatformInterceptorFactory(this, executorName, permissionLevel);
    }

    @Override protected @NotNull UUID resolveOfflinePlayerId(@NotNull String name) {
        var uuid = plugin.getServer().getPlayerUniqueId(name);
        return uuid != null ? uuid : generateOfflinePlayerId(name);
    }

}
