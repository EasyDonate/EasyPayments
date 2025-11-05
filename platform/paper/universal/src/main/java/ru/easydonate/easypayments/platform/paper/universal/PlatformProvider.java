package ru.easydonate.easypayments.platform.paper.universal;

import lombok.Getter;
import org.jetbrains.annotations.Blocking;
import org.jetbrains.annotations.NonBlocking;
import org.jetbrains.annotations.NotNull;
import ru.easydonate.easypayments.core.EasyPayments;
import ru.easydonate.easypayments.core.interceptor.InterceptorFactory;
import ru.easydonate.easypayments.core.platform.provider.PlatformProviderBase;
import ru.easydonate.easypayments.core.platform.scheduler.PlatformScheduler;
import ru.easydonate.easypayments.platform.paper.universal.interceptor.PlatformInterceptorFactory;

import java.util.UUID;

@Getter
public final class PlatformProvider extends PlatformProviderBase {

    private final @NotNull String name;

    public PlatformProvider(
            @NotNull EasyPayments plugin,
            @NotNull PlatformScheduler scheduler,
            @NotNull String executorName,
            int permissionLevel,
            boolean runningFolia
    ) {
        super(plugin, scheduler, executorName, permissionLevel);
        this.name = runningFolia ? "Folia Universal" : "Paper Universal";
    }

    @Override
    @NonBlocking
    protected @NotNull InterceptorFactory interceptorFactoryOf(@NotNull String executorName, int permissionLevel) {
        return new PlatformInterceptorFactory(this, executorName, permissionLevel);
    }

    @Override
    @Blocking
    protected @NotNull UUID resolveOfflinePlayerId(@NotNull String name) {
        var uuid = plugin.getServer().getPlayerUniqueId(name);
        return uuid != null ? uuid : generateOfflinePlayerId(name);
    }

}
