package ru.easydonate.easypayments.gui.item.type.resolver;

import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.easydonate.easypayments.exception.MenuItemParseException;
import ru.easydonate.easypayments.gui.item.type.ItemType;

import java.util.LinkedHashSet;
import java.util.Set;

final class ItemTypeResolverChain {

    private final Set<ResolverMethod> resolverMethods;

    public ItemTypeResolverChain() {
        this.resolverMethods = new LinkedHashSet<>();
    }

    public @NotNull ItemTypeResolverChain append(@NotNull ResolverMethod method) {
        resolverMethods.add(method);
        return this;
    }

    public @Nullable ItemType iterateForResult(@NotNull ConfigurationSection config) throws MenuItemParseException {
        for(ResolverMethod resolverMethod : resolverMethods) {
            ItemType resolved = resolverMethod.resolve(config);
            if(resolved != null) {
                return resolved;
            }
        }

        return null;
    }

    @FunctionalInterface
    interface ResolverMethod {

        @Nullable ItemType resolve(@NotNull ConfigurationSection config) throws MenuItemParseException;

    }

}
