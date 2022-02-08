package ru.easydonate.easypayments.config;

import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.easydonate.easypayments.exception.ConfigurationValidationException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Getter
public abstract class AbstractConfiguration<C extends AbstractConfiguration<C>> {

    protected final Plugin plugin;

    protected InputStream resource;
    protected FileConfiguration bukkitConfig;

    public AbstractConfiguration(@NotNull Plugin plugin) {
        this.plugin = plugin;
    }

    protected abstract @NotNull C getThis();

    public abstract @NotNull String getFileName();

    public abstract @NotNull String getResourcePath();

    public @NotNull C reload() throws ConfigurationValidationException {
        String fileName = getFileName();
        String resourcePath = getResourcePath();

        this.resource = plugin.getClass().getResourceAsStream(resourcePath);
        if(resource == null)
            throw new IllegalArgumentException("Cannot find a resource by path: " + resourcePath);

        Path dataFolder = plugin.getDataFolder().toPath();
        Path outputFile = dataFolder.resolve(fileName.replace('/', File.separatorChar));

        try {
            if(!Files.isRegularFile(outputFile)) {
                Files.createDirectories(outputFile.getParent());
                Files.copy(resource, outputFile, StandardCopyOption.REPLACE_EXISTING);
            }

            this.bukkitConfig = YamlConfiguration.loadConfiguration(outputFile.toFile());
        } catch (IOException ex) {
            plugin.getLogger().severe("Failed to load configuration file '" + fileName + "': " + ex.getMessage());
        }

        return getThis();
    }

    public static @Nullable String colorize(@Nullable String input) {
        return input != null && !input.isEmpty() ? ChatColor.translateAlternateColorCodes('&', input) : input;
    }

    public static @Nullable List<String> colorize(@Nullable List<String> input) {
        return input != null && !input.isEmpty() ? input.stream().map(AbstractConfiguration::colorize).collect(Collectors.toList()) : input;
    }

    public static @Nullable String format(@Nullable String input, @Nullable Object... args) {
        if(input == null || input.isEmpty())
            return input;

        if(args == null || args.length == 0)
            return input;

        String output = input;
        int length = args.length;

        // 'k1 k2' + [k1, v1, k2, v2] -> 'v1 v2'
        for(int i = 0; i < length; i += 2) {
            if(i == length - 1)
                break;

            Object rawKey = args[i];
            if(rawKey == null)
                continue;

            String key = String.valueOf(rawKey);
            String value = String.valueOf(args[i + 1]);
            output = output.replace(key, value);
        }

        return output;
    }

    public @Nullable ConfigurationSection getSection(@NotNull String path) {
        return bukkitConfig.getConfigurationSection(path);
    }

    public @Nullable String getColoredString(@NotNull String path) {
        return colorize(getString(path));
    }

    public @Nullable String getColoredString(@NotNull String path, @Nullable String def) {
        return colorize(getString(path, def));
    }

    public @Nullable String getColoredString(@NotNull String path, @Nullable Supplier<String> def) {
        return colorize(getString(path, def));
    }

    public @Nullable String getString(@NotNull String path) {
        return bukkitConfig.getString(path);
    }

    public @Nullable String getString(@NotNull String path, @Nullable String def) {
        return bukkitConfig.getString(path, def);
    }

    public @Nullable String getString(@NotNull String path, @NotNull Supplier<String> def) {
        String value = bukkitConfig.getString(path);
        return value != null ? value : def.get();
    }

    public boolean getBoolean(@NotNull String path) {
        return bukkitConfig.getBoolean(path);
    }

    public boolean getBoolean(@NotNull String path, boolean def) {
        return bukkitConfig.getBoolean(path, def);
    }

    public int getInt(@NotNull String path) {
        return bukkitConfig.getInt(path);
    }

    public int getInt(@NotNull String path, int def) {
        return bukkitConfig.getInt(path, def);
    }

    public double getDouble(@NotNull String path) {
        return bukkitConfig.getDouble(path);
    }

    public double getDouble(@NotNull String path, double def) {
        return bukkitConfig.getDouble(path, def);
    }

}
