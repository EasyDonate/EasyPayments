package ru.easydonate.easypayments.gui;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

public final class MenuHolder implements InventoryHolder {

    private final Menu menu;

    @Override
    public @NotNull Inventory getInventory() {
        return null;
    }

}
