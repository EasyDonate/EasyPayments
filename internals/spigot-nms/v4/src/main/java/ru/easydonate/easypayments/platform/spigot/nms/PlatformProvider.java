package ru.easydonate.easypayments.platform.spigot.nms;

import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import ru.easydonate.easypayments.core.interceptor.InterceptorFactory;
import ru.easydonate.easypayments.core.platform.provider.PlatformProviderBase;
import ru.easydonate.easypayments.platform.spigot.nms.interceptor.PlatformInterceptorFactory;

public final class PlatformProvider extends PlatformProviderBase {

    public PlatformProvider(@NotNull Plugin plugin, @NotNull String executorName, int permissionLevel) {
        super(plugin, executorName, permissionLevel);
    }

    public static @NotNull Builder builder() {
        return new Builder(PlatformProvider::new);
    }

    @Override
    protected @NotNull InterceptorFactory createInterceptorFactory(@NotNull String executorName, int permissionLevel) {
        return new PlatformInterceptorFactory(this, executorName, permissionLevel);
    }

}
