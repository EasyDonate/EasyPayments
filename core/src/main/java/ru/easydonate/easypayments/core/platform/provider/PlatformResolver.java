package ru.easydonate.easypayments.core.platform.provider;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.jetbrains.annotations.NotNull;
import ru.easydonate.easypayments.core.EasyPayments;
import ru.easydonate.easypayments.core.logging.DebugLogger;
import ru.easydonate.easypayments.core.platform.MinecraftVersion;
import ru.easydonate.easypayments.core.platform.UnsupportedPlatformException;
import ru.easydonate.easypayments.core.platform.scheduler.PlatformScheduler;
import ru.easydonate.easypayments.core.platform.scheduler.bukkit.BukkitPlatformScheduler;
import ru.easydonate.easypayments.core.util.Reflection;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Getter
public final class PlatformResolver {

    public static final MinecraftVersion MINECRAFT_VERSION =  MinecraftVersion.getCurrentVersion();

    private static final Pattern CRAFTBUKKIT_PACKAGE_PATTERN = Pattern.compile("org\\.bukkit\\.craftbukkit\\.v(\\w+)");
    private static final String FOLIA_DETECT_CLASS_NAME = "io.papermc.paper.threadedregions.RegionizedServer";
    private static final String FOLIA_SCHEDULER_CLASS_NAME = "ru.easydonate.easypayments.platform.folia.FoliaPlatformScheduler";
    private static final String NATIVE_INTERCEPTOR_CLASS_NAME = "io.papermc.paper.commands.FeedbackForwardingSender";

    private static final String PAPER_INTERNALS_PLATFORM_CLASS = "paper.internals.PlatformProvider";
    private static final String PAPER_UNIVERSAL_PLATFORM_CLASS = "paper.universal.PlatformProvider";
    private static final String SPIGOT_INTERNALS_PLATFORM_CLASS = "spigot.v%s.PlatformProvider";

    private final EasyPayments plugin;
    private final DebugLogger debugLogger;
    private final EnvironmentLookupResult lookupResult;

    public PlatformResolver(@NotNull EasyPayments plugin, @NotNull DebugLogger debugLogger) throws UnsupportedPlatformException {
        this.plugin = plugin;
        this.debugLogger = debugLogger;
        this.lookupResult = lookupEnvironment(debugLogger);
    }

    public PlatformProvider resolve(@NotNull String username, int permissionLevel) throws UnsupportedPlatformException {
        List<String> candidates = resolvePlatformCandidates(lookupResult);
        String candidatesJoined = !candidates.isEmpty() ? String.join(", ", candidates) : "<nothing>";
        debugLogger.debug("[Platform] Candidates ({0}): {1}", candidates.size(), candidatesJoined);

        Iterator<String> iterator = candidates.iterator();
        while (iterator.hasNext()) {
            String platformClassName = iterator.next();
            Class<?> platformClass;

            try {
                platformClass = Class.forName(platformClassName);
            } catch (ClassNotFoundException ex) {
                debugLogger.debug("[Platform] Platform class '{0}' not found!", platformClassName);
                if (iterator.hasNext())
                    continue;

                throw new UnsupportedPlatformException("platform implementation class not found", ex);
            } catch (Throwable ex) {
                debugLogger.debug("[Platform] Platform class '{0}' cannot be loaded!", platformClassName);
                if (iterator.hasNext())
                    continue;

                throw new UnsupportedPlatformException("platform implementation class cannot be loaded", ex);
            }

            PlatformScheduler scheduler = resolveScheduler();

            try {
                // Paper platforms
                return (PlatformProvider) platformClass
                        .getConstructor(EasyPayments.class, PlatformScheduler.class, String.class, int.class, boolean.class)
                        .newInstance(plugin, scheduler, username, permissionLevel, lookupResult.isFoliaDetected());
            } catch (Throwable ignored) {
                try {
                    // Spigot platform
                    return (PlatformProvider) platformClass
                            .getConstructor(EasyPayments.class, PlatformScheduler.class, String.class, int.class)
                            .newInstance(plugin, scheduler, username, permissionLevel);
                } catch (Throwable ex) {
                    throw new UnsupportedPlatformException("couldn't create platform implementation instance", ex);
                }
            }
        }

        throw new UnsupportedPlatformException("seems that here is no supported platform");
    }

    private PlatformScheduler resolveScheduler() throws UnsupportedPlatformException {
        if (lookupResult.isFoliaDetected()) {
            try {
                Class<?> clazz = Class.forName(FOLIA_SCHEDULER_CLASS_NAME);
                return (PlatformScheduler) clazz.getConstructor(Server.class).newInstance(plugin.getServer());
            } catch (Throwable ex) {
                throw new UnsupportedPlatformException("couldn't instantiate Folia platform scheduler", ex);
            }
        }

        return new BukkitPlatformScheduler(plugin.getServer());
    }

    private static List<String> resolvePlatformCandidates(@NotNull EnvironmentLookupResult lookupResult) throws UnsupportedPlatformException {
        Set<String> candidates = new LinkedHashSet<>();
        if (lookupResult.isFoliaDetected()) {
            if (!lookupResult.isNativeInterceptorSupported())
                throw new UnsupportedPlatformException("unsupported Folia build detected");

            if (!MINECRAFT_VERSION.isAtLeast(MinecraftVersion.FOLIA_SUPPORTED_UPDATE))
                throw new UnsupportedPlatformException("unsupported Folia version detected");

            candidates.add(PAPER_UNIVERSAL_PLATFORM_CLASS);
        }

        if (lookupResult.isNativeInterceptorSupported())
            candidates.add(PAPER_UNIVERSAL_PLATFORM_CLASS);

        if (lookupResult.isUnrelocatedInternalsDetected())
            candidates.add(PAPER_INTERNALS_PLATFORM_CLASS);

        if (lookupResult.getInternalsVersion() != null)
            candidates.add(String.format(SPIGOT_INTERNALS_PLATFORM_CLASS, lookupResult.getInternalsVersion()));

        return candidates.stream()
                .map(str -> "ru.easydonate.easypayments.platform." + str)
                .collect(Collectors.toList());
    }

    private static EnvironmentLookupResult lookupEnvironment(@NotNull DebugLogger logger) throws UnsupportedPlatformException {
        String craftBukkitPackage = Bukkit.getServer().getClass().getPackage().getName();
        logger.debug("[Platform] CraftBukkit package: '{0}'", craftBukkitPackage);

        boolean foliaDetected = detectFolia();
        logger.debug("[Platform] Folia detected: {0}", foliaDetected);

        boolean nativeInterceptorSupported = detectNativeInterceptorSupport();
        logger.debug("[Platform] Native interceptor supported: {0}", nativeInterceptorSupported);

        if ("org.bukkit.craftbukkit".equals(craftBukkitPackage)) {
            logger.info("[Platform] Detected unrelocated internals (MC {0})", MINECRAFT_VERSION.getVersion());
            return new EnvironmentLookupResult(null, true, foliaDetected, nativeInterceptorSupported);
        }

        Matcher matcher = CRAFTBUKKIT_PACKAGE_PATTERN.matcher(craftBukkitPackage);
        if (matcher.find()) {
            String internalsVersion = matcher.group(1);
            logger.info("[Platform] Detected internals version: {0} (MC {1})", internalsVersion, MINECRAFT_VERSION.getVersion());
            return new EnvironmentLookupResult(internalsVersion, false, foliaDetected, nativeInterceptorSupported);
        }

        throw new UnsupportedPlatformException();
    }

    private static boolean detectFolia() {
        try {
            Class.forName(FOLIA_DETECT_CLASS_NAME);
            return true;
        } catch (Exception ignored) {
            return false;
        }
    }

    private static boolean detectNativeInterceptorSupport() {
        try {
            Class.forName(NATIVE_INTERCEPTOR_CLASS_NAME);
            return Reflection.getMethod(Server.class, "createCommandSender", Consumer.class) != null;
        } catch (Exception ignored) {
            return false;
        }
    }

    @Getter
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class EnvironmentLookupResult {
        private final String internalsVersion;
        private final boolean unrelocatedInternalsDetected;
        private final boolean foliaDetected;
        private final boolean nativeInterceptorSupported;
    }

}
