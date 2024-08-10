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
                case "1.21.0":
                case "1.21.1":
                case "1.21.2":
                case "1.21.3":
                    return "1_21_R1";
            }
        }

        return null;
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
                String currentPackage = PlatformProvider.class.getPackage().getName();
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
