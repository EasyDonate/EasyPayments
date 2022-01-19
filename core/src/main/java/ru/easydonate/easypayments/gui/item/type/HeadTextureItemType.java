package ru.easydonate.easypayments.gui.item.type;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.easydonate.easypayments.nms.NMSHelper;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class HeadTextureItemType implements ItemType {

    private final String headTexture;

    public static @Nullable HeadTextureItemType parse(@NotNull String headTexture) {
        return headTexture != null ? new HeadTextureItemType(headTexture) : null;
    }

    @Override
    public @NotNull ItemStack createItemStack(@NotNull NMSHelper nmsHelper) {
        ItemStack bukkitItem = new ItemStack(HEAD_MATERIAL_CONSTANT);
        return nmsHelper.createNotchianItemWrapper(bukkitItem)
                .setHeadTexture(headTexture)
                .copyAsModifiedItem();
    }

}
