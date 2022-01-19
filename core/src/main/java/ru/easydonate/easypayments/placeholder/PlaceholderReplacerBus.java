package ru.easydonate.easypayments.placeholder;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashSet;
import java.util.Set;

public final class PlaceholderReplacerBus {

    private final Set<PlaceholderReplacer> replacers;

    public PlaceholderReplacerBus() {
        this.replacers = new LinkedHashSet<>();
    }

    public @NotNull StringContainer processPlaceholders(@NotNull Player bukkitPlayer, @NotNull StringContainer container) {
        if(!replacers.isEmpty() && container.isSupportingPlaceholders())
            for(PlaceholderReplacer replacer : replacers)
                replacer.replace(bukkitPlayer, container);

        return container;
    }

    public @NotNull PlaceholderReplacerBus addReplacer(@NotNull PlaceholderReplacer replacer) {
        replacers.add(replacer);
        return this;
    }

    public @NotNull PlaceholderReplacerBus removeReplacer(@NotNull PlaceholderReplacer replacer) {
        replacers.remove(replacer);
        return this;
    }

}
