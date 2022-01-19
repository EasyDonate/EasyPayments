package ru.easydonate.easypayments.gui.item.type.resolver;

import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.easydonate.easypayments.exception.MenuItemParseException;
import ru.easydonate.easypayments.gui.item.type.ItemType;
import ru.easydonate.easypayments.nms.MinecraftVersion;

public interface ItemTypeResolver {

    static @NotNull ItemTypeResolver getRelevantResolver() {
        if(MinecraftVersion.atOrAbove(MinecraftVersion.AQUATIC_UPDATE))
            return new ModernItemTypeResolver();
        else
            return new LegacyItemTypeResolver();
    }

    @Nullable ItemType resolve(@NotNull ConfigurationSection config, boolean required) throws MenuItemParseException;

}
