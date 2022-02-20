package ru.easydonate.easypayments.nms.provider.v1_8_R1;

import org.bukkit.craftbukkit.v1_8_R1.scheduler.CraftTask;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import ru.easydonate.easypayments.execution.interceptor.InterceptorFactory;
import ru.easydonate.easypayments.nms.provider.AbstractVersionedFeaturesProvider;
import ru.easydonate.easypayments.nms.provider.v1_8_R1.interceptor.VersionedInterceptorFactory;
import ru.easydonate.easypayments.utility.Reflection;

import java.lang.reflect.Method;
import java.util.Optional;

public final class VersionedFeaturesProvider extends AbstractVersionedFeaturesProvider {

    private static final Method getPeriod = Reflection.getDeclaredMethod(CraftTask.class, "getPeriod");

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

    @Override
    public boolean isTaskCancelled(@NotNull BukkitTask bukkitTask) {
        if(!(bukkitTask instanceof CraftTask))
            throw new IllegalArgumentException("this bukkit task isn't a CraftTask instance!");

        Optional<Long> result = Reflection.invokeMethod(getPeriod, bukkitTask);
        return !result.isPresent() || result.get() == -2L;
    }

}
