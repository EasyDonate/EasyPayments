package ru.easydonate.easypayments.core.config;

import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.easydonate.easypayments.core.exception.ConfigurationValidationException;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public interface Configuration {

    String getName();

    ConfigurationValidator getValidator();

    Map<String, Object> getOverrides();

    void setValidator(@Nullable ConfigurationValidator validator);

    default void registerKeyAliases(@NotNull String key, String... aliases) {
        throw new UnsupportedOperationException("This configuration doesn't support key aliases!");
    }

    boolean reload() throws ConfigurationValidationException;

    @Nullable ConfigurationSection getSection(@NotNull String path);

    @Nullable String getColoredString(@NotNull String path);

    @Nullable String getColoredString(@NotNull String path, @Nullable String def);

    @Nullable String getColoredString(@NotNull String path, @Nullable Supplier<String> def);

    @Nullable String getString(@NotNull String path);

    @Nullable String getString(@NotNull String path, @Nullable String def);

    @Nullable String getString(@NotNull String path, @NotNull Supplier<String> def);

    @NotNull List<String> getStringList(@NotNull String path);

    boolean getBoolean(@NotNull String path);

    boolean getBoolean(@NotNull String path, boolean def);

    int getInt(@NotNull String path);

    int getInt(@NotNull String path, int def);

    int getIntWithBounds(@NotNull String path, int min, int max);

    int getIntWithBounds(@NotNull String path, int min, int max, int def);

    @NotNull List<Integer> getIntList(@NotNull String path);

    double getDouble(@NotNull String path);

    double getDouble(@NotNull String path, double def);

}
