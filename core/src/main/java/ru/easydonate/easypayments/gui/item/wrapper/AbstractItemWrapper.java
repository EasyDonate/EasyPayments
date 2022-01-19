package ru.easydonate.easypayments.gui.item.wrapper;

import org.jetbrains.annotations.NotNull;
import ru.easydonate.easypayments.nms.NMSHelper;

public abstract class AbstractItemWrapper implements NotchianItemWrapper {

    public static final String HEAD_TEXTURE_JSON_PATTERN = "{\"textures\":{\"SKIN\":{\"url\":\"http://textures.minecraft.net/texture/%s\"}}}";
    public static final String CURRENT_VERSION_X = String.format("%d.%d.X", NMSHelper.MINECRAFT_VERSION.getMajor(), NMSHelper.MINECRAFT_VERSION.getMinor());

    @Override
    public @NotNull NotchianItemWrapper setHeadOwner(@NotNull String headOwner) {
        return setHeadOwner(headOwner, null);
    }

    @Override
    public @NotNull NotchianItemWrapper setHeadData(@NotNull String base64) {
        return setHeadData(base64, null);
    }

    @Override
    public @NotNull NotchianItemWrapper setHeadTexture(@NotNull String textureId) {
        return setHeadData(String.format(HEAD_TEXTURE_JSON_PATTERN, textureId));
    }

    @Override
    public @NotNull NotchianItemWrapper setCustomModelData(int customModelData) throws UnsupportedOperationException {
        throw new UnsupportedOperationException("Custom Model Data item feature isn't supported on MC " + CURRENT_VERSION_X + "!");
    }

}
