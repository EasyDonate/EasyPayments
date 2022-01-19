package ru.easydonate.easypayments.gui.item.type.resolver;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.easydonate.easypayments.exception.MenuItemParseException;
import ru.easydonate.easypayments.gui.item.type.MaterialItemType;

final class ModernItemTypeResolver extends AbstractItemTypeResolver {

    @Override
    protected @Nullable MaterialItemType resolveMaterialItemType(@NotNull ConfigurationSection config) throws MenuItemParseException {
        Material material = resolveMaterialType(config);
        return MaterialItemType.create(material, null);
    }

    private @Nullable Material resolveMaterialType(@NotNull ConfigurationSection config) throws MenuItemParseException {
        if(config.isString(TYPE_PARAMETER)) {
            String value = config.getString(TYPE_PARAMETER);
            Material match = Material.matchMaterial(value);
            if(match == null) {
                throw new MenuItemParseException(config, "Unknown Bukkit material type '%s'!", value);
            }
        }

        throw new MenuItemParseException(config, "The 'type' parameter isn't specified or specified incorrectly!");
    }

}
