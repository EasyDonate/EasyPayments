package ru.easydonate.easypayments.nms;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import ru.easydonate.easypayments.execution.InterceptorFactory;

import java.lang.reflect.Constructor;

public interface NMSHelper extends InterceptorFactory {

    @NotNull MinecraftVersion MINECRAFT_VERSION =  MinecraftVersion.getCurrentVersion();

    @NotNull String NMS_VERSION = getNMSVersion();
    @NotNull String NMS_IMPL_PATTERN = "%s.proxy.v%s.NMSHelperImpl";

    static @NotNull String getNMSVersion() {
        String packageName = Bukkit.getServer().getClass().getPackage().getName();
        return packageName.substring(packageName.lastIndexOf('.') + 2);
    }

    static @NotNull NMSHelper resolve(@NotNull Plugin plugin, @NotNull String username, int permissionLevel) throws UnsupportedVersionException {
        plugin.getLogger().info(String.format("Detected NMS version: %s (MC %s)", NMS_VERSION, MINECRAFT_VERSION.getVersion()));

        String currentPackage = NMSHelper.class.getPackage().getName();
        try {
            Class<?> nmsClass = Class.forName(String.format(NMS_IMPL_PATTERN, currentPackage, NMS_VERSION));
            Constructor<?> constructor = nmsClass.getConstructor(String.class, int.class);
            return (NMSHelper) constructor.newInstance(username, permissionLevel);
        } catch (Throwable ex) {
            throw new UnsupportedVersionException(NMS_VERSION);
        }
    }

}
