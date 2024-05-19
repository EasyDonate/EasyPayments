package ru.easydonate.easypayments.nms.provider;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.easydonate.easypayments.execution.interceptor.InterceptorFactory;
import ru.easydonate.easypayments.nms.MinecraftVersion;
import ru.easydonate.easypayments.nms.UnsupportedVersionException;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public interface VersionedFeaturesProvider {

    @NotNull MinecraftVersion MINECRAFT_VERSION =  MinecraftVersion.getCurrentVersion();

    @Nullable String NMS_VERSION = resolveNMSVersion();
    @NotNull String NMS_IMPL_PATTERN = "%s.v%s.VersionedFeaturesProvider";

    static @Nullable String resolveNMSVersion() {
        String packageName = Bukkit.getServer().getClass().getPackage().getName();

        Pattern regex = Pattern.compile("org\\.bukkit\\.craftbukkit\\.v(\\w+)");
        Matcher matcher = regex.matcher(packageName);
        if (matcher.find())
            return matcher.group(1);

        String version = MINECRAFT_VERSION.getVersion();
        if (version != null) {
            switch (version) {
                case "1.20.5":
                case "1.20.6":
                    return "1_20_R4";
            }
        }

        return null;
    }

    static @NotNull VersionedFeaturesProvider create(@NotNull Plugin plugin, @NotNull String executorName, int permissionLevel) throws UnsupportedVersionException {
        try {
            Class<?> clazz = resolveImplementationClass(plugin);
            Constructor<?> constructor = clazz.getConstructor(Plugin.class, String.class, int.class);
            return (VersionedFeaturesProvider) constructor.newInstance(plugin, executorName, permissionLevel);
        } catch (Throwable ex) {
            throw new UnsupportedVersionException(NMS_VERSION);
        }
    }

    static @NotNull Builder builder(@NotNull Plugin plugin) throws UnsupportedVersionException {
        try {
            Class<?> clazz = resolveImplementationClass(plugin);
            Method method = clazz.getMethod("builder");

            Builder builder = (Builder) method.invoke(null);
            return builder.withPlugin(plugin);
        } catch (Throwable ex) {
            throw new UnsupportedVersionException(NMS_VERSION);
        }
    }

    static @NotNull Class<?> resolveImplementationClass(@NotNull Plugin plugin) throws UnsupportedVersionException {
        if (NMS_VERSION != null) {
            plugin.getLogger().info(String.format("Detected NMS version: %s (MC %s)", NMS_VERSION, MINECRAFT_VERSION.getVersion()));

            try {
                String currentPackage = VersionedFeaturesProvider.class.getPackage().getName();
                return Class.forName(String.format(NMS_IMPL_PATTERN, currentPackage, NMS_VERSION));
            } catch (Throwable ignored) {
            }
        } else {
            plugin.getLogger().warning(String.format("Unable to detect NMS version! (MC %s)", MINECRAFT_VERSION.getVersion()));
        }

        throw new UnsupportedVersionException(NMS_VERSION);
    }

    @NotNull Plugin getPlugin();

    @NotNull String getMinecraftVersion();

    @NotNull String getNMSVersion();

    @NotNull InterceptorFactory getInterceptorFactory();

    boolean isTaskCancelled(@NotNull BukkitTask bukkitTask);

    interface Builder {

        @NotNull VersionedFeaturesProvider create();

        @NotNull Builder withPlugin(@NotNull Plugin plugin);

        @NotNull Builder withExecutorName(@NotNull String executorName);

        @NotNull Builder withPermissionLevel(int permissionLevel);

    }

}
