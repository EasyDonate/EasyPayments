package ru.easydonate.easypayments.exception;

import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class MenuItemParseException extends Exception {

    private static final String MESSAGE_FORMAT = "Couldn't parse a menu item from '%s': %s";

    public MenuItemParseException(@NotNull ConfigurationSection config, @NotNull String message, @Nullable Object... args) {
        super(String.format(MESSAGE_FORMAT, config.getCurrentPath(), String.format(message, args)));
    }

}
