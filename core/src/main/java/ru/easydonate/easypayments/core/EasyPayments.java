package ru.easydonate.easypayments.core;

import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import ru.easydonate.easypayments.core.logging.DebugLogger;

public interface EasyPayments extends Plugin {

    @NotNull DebugLogger getDebugLogger();

}
