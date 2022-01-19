package ru.easydonate.easypayments.placeholder;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface PlaceholderReplacer {

    void replace(@NotNull Player bukkitPlayer, @NotNull StringContainer container);

}
