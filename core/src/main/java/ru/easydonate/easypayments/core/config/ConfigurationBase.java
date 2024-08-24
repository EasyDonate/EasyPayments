package ru.easydonate.easypayments.core.config;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.easydonate.easypayments.core.exception.ConfigurationValidationException;
import ru.easydonate.easypayments.core.formatting.StringFormatter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

@Getter @Setter
public abstract class ConfigurationBase implements Configuration {

    private final Map<String, Object> overrides;
    private ConfigurationValidator validator;

    public ConfigurationBase() {
        this.overrides = new HashMap<>();
    }

    protected abstract @NotNull FileConfiguration getBukkitConfiguration();

    protected final void validate() throws ConfigurationValidationException {
        if (validator != null) {
            validator.validate(this);
        }
    }

    protected final void resetOverrides() {
        overrides.clear();
    }

    @Override
    public @Nullable ConfigurationSection getSection(@NotNull String path) {
        return getBukkitConfiguration().getConfigurationSection(path);
    }

    @Override
    public @Nullable String getColoredString(@NotNull String path) {
        return StringFormatter.colorize(getString(path));
    }

    @Override
    public @Nullable String getColoredString(@NotNull String path, @Nullable String def) {
        return StringFormatter.colorize(getString(path, def));
    }

    @Override
    public @Nullable String getColoredString(@NotNull String path, @Nullable Supplier<String> def) {
        return StringFormatter.colorize(getString(path, def));
    }

    @Override
    public @Nullable String getString(@NotNull String path) {
        return getBukkitConfiguration().getString(path);
    }

    @Override
    public @Nullable String getString(@NotNull String path, @Nullable String def) {
        return getBukkitConfiguration().getString(path, def);
    }

    @Override
    public @Nullable String getString(@NotNull String path, @NotNull Supplier<String> def) {
        String value = getBukkitConfiguration().getString(path);
        return value != null ? value : def.get();
    }

    @Override
    public @NotNull List<String> getStringList(@NotNull String path) {
        return getBukkitConfiguration().getStringList(path);
    }

    @Override
    public boolean getBoolean(@NotNull String path) {
        return getBukkitConfiguration().getBoolean(path);
    }

    @Override
    public boolean getBoolean(@NotNull String path, boolean def) {
        return getBukkitConfiguration().getBoolean(path, def);
    }

    @Override
    public int getInt(@NotNull String path) {
        return getBukkitConfiguration().getInt(path);
    }

    @Override
    public int getInt(@NotNull String path, int def) {
        return getBukkitConfiguration().getInt(path, def);
    }

    @Override
    public int getIntWithBounds(@NotNull String path, int min, int max) {
        return Math.min(Math.max(getInt(path), min), max);
    }

    @Override
    public int getIntWithBounds(@NotNull String path, int min, int max, int def) {
        return Math.min(Math.max(getInt(path, def), min), max);
    }

    @Override
    public @NotNull List<Integer> getIntList(@NotNull String path) {
        return getBukkitConfiguration().getIntegerList(path);
    }

    @Override
    public double getDouble(@NotNull String path) {
        return getBukkitConfiguration().getDouble(path);
    }

    @Override
    public double getDouble(@NotNull String path, double def) {
        return getBukkitConfiguration().getDouble(path, def);
    }

}
