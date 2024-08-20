package ru.easydonate.easypayments.core.logging;

import lombok.experimental.UtilityClass;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.plugin.Plugin;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;

@UtilityClass
public class DebugEnvironmentLookup {

    public static void writeEnvironmentInfo(Plugin plugin, DebugLogger logger) {
        Server server = Bukkit.getServer();

        String osName = System.getProperty("os.name");
        String osVersion = System.getProperty("os.version");
        String osArch = System.getProperty("os.arch");

        String javaSpecVersion = System.getProperty("java.vm.specification.version");
        String javaVmName = System.getProperty("java.vm.name");
        String javaVmVersion = System.getProperty("java.vm.version");

        logger.writeToFile(Arrays.asList(
                "****************************************************************",
                String.format("* EasyPayments  v%s", plugin.getDescription().getVersion()),
                String.format("* Server        %s - %s", server.getName(), server.getVersion()),
                String.format("* Runtime       Java %s (%s, %s)", javaSpecVersion, javaVmName, javaVmVersion),
                String.format("* System        %s %s (%s)", osName, osVersion, osArch),
                String.format("* Timestamp     %s", DebugLogger.LOG_TIMESTAMP_FORMATTER.format(LocalDateTime.now(ZoneOffset.UTC))),
                "****************************************************************"
        ));
    }

}
