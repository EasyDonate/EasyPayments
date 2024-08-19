package ru.easydonate.easypayments.core.platform.provider;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.easydonate.easypayments.core.interceptor.InterceptorFactory;
import ru.easydonate.easypayments.core.platform.MinecraftVersion;
import ru.easydonate.easypayments.core.platform.UnsupportedVersionException;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public interface PlatformProvider {

    @NotNull MinecraftVersion MINECRAFT_VERSION =  MinecraftVersion.getCurrentVersion();

    @Nullable String NMS_VERSION = resolveNMSVersion();
    @NotNull String SPIGOT_NMS_PLATFORM_PATTERN = "ru.easydonate.easypayments.platform.spigot.nms.v%s.PlatformProvider";

    static @Nullable String resolveNMSVersion() {
        String packageName = Bukkit.getServer().getClass().getPackage().getName();
        Pattern regex = Pattern.compile("org\\.bukkit\\.craftbukkit\\.v(\\w+)");
        Matcher matcher = regex.matcher(packageName);
        return matcher.find() ? matcher.group(1) : null;
    }

    static @NotNull PlatformProvider create(@NotNull Plugin plugin, @NotNull String executorName, int permissionLevel) throws UnsupportedVersionException {
        try {
            Class<?> clazz = resolveImplementationClass(plugin);
            Constructor<?> constructor = clazz.getConstructor(Plugin.class, String.class, int.class);
            return (PlatformProvider) constructor.newInstance(plugin, executorName, permissionLevel);
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
                return Class.forName(String.format(SPIGOT_NMS_PLATFORM_PATTERN, NMS_VERSION));
            } catch (Throwable ignored) {
            }
        } else {
            plugin.getLogger().warning(String.format("Unable to detect NMS version! (MC %s)", MINECRAFT_VERSION.getVersion()));
        }

        throw new UnsupportedVersionException(NMS_VERSION);
    }

    @NotNull Plugin getPlugin();

    @NotNull String getMinecraftVersion();

    @Nullable String getNMSVersion();

    @NotNull InterceptorFactory getInterceptorFactory();

    boolean isTaskCancelled(@NotNull BukkitTask bukkitTask);

    interface Builder {

        @NotNull PlatformProvider create();

        @NotNull Builder withPlugin(@NotNull Plugin plugin);

        @NotNull Builder withExecutorName(@NotNull String executorName);

        @NotNull Builder withPermissionLevel(int permissionLevel);

    }

}
