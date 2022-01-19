package ru.easydonate.easypayments.gui.item.type;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.easydonate.easypayments.nms.NMSHelper;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class MaterialItemType implements ItemType {

    private final Material bukkitType;
    private final Byte bukkitData;

    public static @Nullable MaterialItemType create(@Nullable Material material, @Nullable Byte data) {
        return material != null ? new MaterialItemType(material, data) : null;
    }

    @SuppressWarnings("deprecation")
    @Override
    public @NotNull ItemStack createItemStack(@NotNull NMSHelper nmsHelper) {
        return bukkitData != null
                ? new ItemStack(bukkitType, 1, (short) 0, bukkitData)
                : new ItemStack(bukkitType);
    }

}
