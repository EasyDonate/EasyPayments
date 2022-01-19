package ru.easydonate.easypayments.nms.proxy.v1_16_R1;

import com.google.common.util.concurrent.Futures;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import lombok.Getter;
import net.minecraft.server.v1_16_R1.*;
import org.bukkit.craftbukkit.v1_16_R1.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.easydonate.easypayments.gui.item.wrapper.AbstractItemWrapper;
import ru.easydonate.easypayments.gui.item.wrapper.NotchianItemWrapper;

import java.util.Optional;
import java.util.OptionalInt;
import java.util.UUID;

@Getter
public final class NMSItemWrapper extends AbstractItemWrapper {

    private final ItemStack bukkitItem;
    private final net.minecraft.server.v1_16_R1.ItemStack nmsItem;

    public NMSItemWrapper(@NotNull ItemStack bukkitItem) {
        this.bukkitItem = bukkitItem;
        this.nmsItem = CraftItemStack.asNMSCopy(bukkitItem);
    }

    @Override
    public @NotNull ItemStack copyAsModifiedItem() {
        return CraftItemStack.asBukkitCopy(nmsItem);
    }

    @Override
    public @NotNull Object getNMSItem() {
        return nmsItem;
    }

    @Override
    public @NotNull NotchianItemWrapper setHeadOwner(@NotNull String headOwner, @Nullable UUID ownerUUID) {
        GameProfile gameProfile = new GameProfile(ownerUUID, headOwner);
        setGameProfile(gameProfile, true);
        return this;
    }

    @Override
    public @NotNull NotchianItemWrapper setHeadData(@NotNull String headData, @Nullable String signature) {
        GameProfile gameProfile = new GameProfile(null, "");

        PropertyMap properties = gameProfile.getProperties();
        Property textures = new Property("textures", headData, signature);
        properties.put("textures", textures);

        setGameProfile(gameProfile, false);
        return this;
    }

    @Override
    public @NotNull NotchianItemWrapper setCustomModelData(int customModelData) {
        return setNbtInt(CUSTOM_MODEL_DATA_NBT, customModelData);
    }

    @Override
    public @NotNull Optional<String> getNbtString(@NotNull String key) {
        NBTBase nbtTag = getNbtTag(key);
        return nbtTag instanceof NBTTagString ? Optional.of(nbtTag.asString()) : Optional.empty();
    }

    @Override
    public @NotNull OptionalInt getNbtInt(@NotNull String key) {
        NBTBase nbtTag = getNbtTag(key);
        return nbtTag instanceof NBTNumber ? OptionalInt.of(((NBTNumber) nbtTag).asInt()) : OptionalInt.empty();
    }

    @Override
    public @NotNull NotchianItemWrapper setNbtString(@NotNull String key, @NotNull String value) {
        getNbtTagCompound().setString(key, value);
        return this;
    }

    @Override
    public @NotNull NotchianItemWrapper setNbtInt(@NotNull String key, int value) {
        getNbtTagCompound().setInt(key, value);
        return this;
    }

    @Override
    public @NotNull NotchianItemWrapper removeNbtTag(@NotNull String key) {
        getNbtTagCompound().remove(key);
        return this;
    }

    private void setGameProfile(@NotNull GameProfile gameProfile, boolean fillTextures) {
        if(fillTextures) {
            GameProfile filledProfile = Futures.getUnchecked(TileEntitySkull.b(gameProfile, p -> true, true));
            NBTTagCompound serializedProfile = GameProfileSerializer.serialize(new NBTTagCompound(), filledProfile);
            getNbtTagCompound().set(SKULL_OWNER_NBT, serializedProfile);
        } else {
            NBTTagCompound serializedProfile = GameProfileSerializer.serialize(new NBTTagCompound(), gameProfile);
            getNbtTagCompound().set(SKULL_OWNER_NBT, serializedProfile);
        }
    }

    private @NotNull NBTTagCompound getNbtTagCompound() {
        return nmsItem.getOrCreateTag();
    }

    private @Nullable NBTBase getNbtTag(@NotNull String key) {
        return getNbtTagCompound().get(key);
    }

}
