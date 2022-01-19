package ru.easydonate.easypayments.gui.item.wrapper;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public interface ItemWrapperFactory {

    @NotNull NotchianItemWrapper createNotchianItemWrapper(@NotNull ItemStack bukkitItem);

}
