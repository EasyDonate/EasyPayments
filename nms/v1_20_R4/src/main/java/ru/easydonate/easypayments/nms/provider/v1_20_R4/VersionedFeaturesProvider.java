package ru.easydonate.easypayments.nms.provider.v1_20_R4;

import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import ru.easydonate.easypayments.execution.interceptor.InterceptorFactory;
import ru.easydonate.easypayments.nms.provider.AbstractVersionedFeaturesProvider;
import ru.easydonate.easypayments.nms.provider.v1_20_R4.interceptor.VersionedInterceptorFactory;

public final class VersionedFeaturesProvider extends AbstractVersionedFeaturesProvider {

    public VersionedFeaturesProvider(@NotNull Plugin plugin, @NotNull String executorName, int permissionLevel) {
        super(plugin, executorName, permissionLevel);
    }

    public static @NotNull Builder builder() {
        return new Builder(VersionedFeaturesProvider::new);
    }

    @Override
    protected @NotNull InterceptorFactory createInterceptorFactory(@NotNull String executorName, int permissionLevel) {
        return new VersionedInterceptorFactory(this, executorName, permissionLevel);
    }

}
