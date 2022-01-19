package ru.easydonate.easypayments.gui.item.type;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import ru.easydonate.easypayments.nms.MinecraftVersion;
import ru.easydonate.easypayments.nms.NMSHelper;

public interface ItemType {

    @NotNull Material HEAD_MATERIAL_CONSTANT = MinecraftVersion.atOrAbove(MinecraftVersion.AQUATIC_UPDATE)
            ? Material.valueOf("PLAYER_HEAD")
            : Material.valueOf("SKULL_ITEM");

    @NotNull ItemStack createItemStack(@NotNull NMSHelper nmsHelper);

}
