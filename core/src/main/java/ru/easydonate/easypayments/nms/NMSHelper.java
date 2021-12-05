package ru.easydonate.easypayments.nms;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import ru.easydonate.easypayments.execution.InterceptorFactory;

import java.lang.reflect.Constructor;

public interface NMSHelper extends InterceptorFactory {

    static String getNMSVersion() {
        String packageName = Bukkit.getServer().getClass().getPackage().getName();
        return packageName.substring(packageName.lastIndexOf('.') + 2);
    }

    static NMSHelper resolve(Plugin plugin, String username, int permissionLevel) throws UnsupportedVersionException {
        String nmsVersion = getNMSVersion();
        plugin.getLogger().info("Detected NMS version: " + nmsVersion);

        String currentPackage = NMSHelper.class.getPackage().getName();
        try {
            Class<?> nmsClass = Class.forName(currentPackage + ".proxy.NMS_" + nmsVersion);
            Constructor<?> constructor = nmsClass.getConstructor(String.class, int.class);
            return (NMSHelper) constructor.newInstance(username, permissionLevel);
        } catch (Throwable ex) {
            throw new UnsupportedVersionException(nmsVersion);
        }
    }

}
