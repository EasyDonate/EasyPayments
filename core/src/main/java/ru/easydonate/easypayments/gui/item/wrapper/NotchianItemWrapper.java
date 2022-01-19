package ru.easydonate.easypayments.gui.item.wrapper;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.OptionalInt;
import java.util.UUID;

public interface NotchianItemWrapper {

    String SKULL_OWNER_NBT = "SkullOwner";
    String CUSTOM_MODEL_DATA_NBT = "CustomModelData";

    @NotNull ItemStack copyAsModifiedItem();

    @NotNull Object getNMSItem();

    @NotNull ItemStack getBukkitItem();

    @NotNull NotchianItemWrapper setHeadOwner(@NotNull String headOwner);

    @NotNull NotchianItemWrapper setHeadOwner(@NotNull String headOwner, @Nullable UUID ownerUUID);

    @NotNull NotchianItemWrapper setHeadData(@NotNull String base64);

    @NotNull NotchianItemWrapper setHeadData(@NotNull String base64, @Nullable String signature);

    @NotNull NotchianItemWrapper setHeadTexture(@NotNull String textureId);

    @NotNull NotchianItemWrapper setCustomModelData(int customModelData) throws UnsupportedOperationException;

    @NotNull Optional<String> getNbtString(@NotNull String key);

    @NotNull OptionalInt getNbtInt(@NotNull String key);

    @NotNull NotchianItemWrapper setNbtString(@NotNull String key, @NotNull String value);

    @NotNull NotchianItemWrapper setNbtInt(@NotNull String key, int value);

    @NotNull NotchianItemWrapper removeNbtTag(@NotNull String key);

}
