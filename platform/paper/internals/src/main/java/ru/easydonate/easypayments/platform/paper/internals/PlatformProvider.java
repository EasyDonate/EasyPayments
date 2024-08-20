package ru.easydonate.easypayments.platform.paper.internals;

import lombok.Getter;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import ru.easydonate.easypayments.core.EasyPayments;
import ru.easydonate.easypayments.core.interceptor.InterceptorFactory;
import ru.easydonate.easypayments.core.platform.provider.PlatformProviderBase;
import ru.easydonate.easypayments.core.platform.scheduler.PlatformScheduler;
import ru.easydonate.easypayments.platform.paper.internals.interceptor.PlatformInterceptorFactory;

@Getter
public final class PlatformProvider extends PlatformProviderBase {

    private final String name;

    public PlatformProvider(
            @NotNull EasyPayments plugin,
            @NotNull PlatformScheduler scheduler,
            @NotNull String executorName,
            int permissionLevel,
            boolean runningFolia
    ) {
        super(plugin, scheduler, executorName, permissionLevel);
        this.name = runningFolia ? "Folia Internals" : "Paper Internals";
    }

    @Override
    protected @NotNull InterceptorFactory createInterceptorFactory(@NotNull String executorName, int permissionLevel) {
        return new PlatformInterceptorFactory(this, executorName, permissionLevel);
    }

}
