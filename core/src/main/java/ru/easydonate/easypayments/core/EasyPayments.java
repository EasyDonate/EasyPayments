package ru.easydonate.easypayments.core;

import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.easydonate.easypayments.core.formatting.RelativeTimeFormatter;
import ru.easydonate.easypayments.core.logging.DebugLogger;
import ru.easydonate.easypayments.core.platform.provider.PlatformProvider;

public interface EasyPayments extends Plugin {

    @NotNull PlatformProvider getPlatformProvider();

    @NotNull DebugLogger getDebugLogger();

    @NotNull RelativeTimeFormatter getRelativeTimeFormatter();

    @Nullable String getAccessKey();

    int getServerId();

    int getPermissionLevel();

}
