package ru.easydonate.easypayments.gui.item.type.resolver;

import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.easydonate.easypayments.exception.MenuItemParseException;
import ru.easydonate.easypayments.gui.item.type.*;

import java.util.function.Function;

abstract class AbstractItemTypeResolver implements ItemTypeResolver {

    public static final String TYPE_PARAMETER = "type";
    public static final String DATA_PARAMETER = "data";
    public static final String HEAD_OWNER_PARAMETER = "head-owner";
    public static final String HEAD_DATA_PARAMETER = "head-data";
    public static final String HEAD_TEXTURE_PARAMETER = "head-texture";

    private final ItemTypeResolverChain resolverChain;

    public AbstractItemTypeResolver() {
        this.resolverChain = new ItemTypeResolverChain()
                .append(this::resolveMaterialItemType)
                .append(this::resolveHeadOwnerItemType)
                .append(this::resolveHeadDataItemType)
                .append(this::resolveHeadTextureItemType);
    }

    @Override
    public @Nullable ItemType resolve(@NotNull ConfigurationSection config, boolean required) throws MenuItemParseException {
        ItemType result = resolverChain.iterateForResult(config);
        if(result == null && required)
            throw new MenuItemParseException(config, "Cannot resolve the menu item type!");

        return result;
    }

    protected abstract @Nullable MaterialItemType resolveMaterialItemType(@NotNull ConfigurationSection config) throws MenuItemParseException;

    protected @Nullable HeadOwnerItemType resolveHeadOwnerItemType(@NotNull ConfigurationSection config) throws MenuItemParseException {
        return resolveHeadRelatedItemType(config, HEAD_OWNER_PARAMETER, HeadOwnerItemType::parse);
    }

    protected @Nullable HeadDataItemType resolveHeadDataItemType(@NotNull ConfigurationSection config) throws MenuItemParseException {
        return resolveHeadRelatedItemType(config, HEAD_DATA_PARAMETER, HeadDataItemType::parse);
    }

    protected @Nullable HeadTextureItemType resolveHeadTextureItemType(@NotNull ConfigurationSection config) throws MenuItemParseException {
        return resolveHeadRelatedItemType(config, HEAD_TEXTURE_PARAMETER, HeadTextureItemType::parse);
    }

    private <T> @Nullable T resolveHeadRelatedItemType(
            @NotNull ConfigurationSection config,
            @NotNull String key,
            @NotNull Function<String, T> instanceCreator
    ) throws MenuItemParseException {
        String value = config.getString(key);
        if(value == null)
            return null;

        if(value.isEmpty())
            throw new MenuItemParseException(config, "A value of '%s' cannot be empty!", key);

        return instanceCreator.apply(value);
    }

}
