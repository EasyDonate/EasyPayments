package ru.easydonate.easypayments.core.config.template;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import ru.easydonate.easypayments.core.EasyPayments;
import ru.easydonate.easypayments.core.config.ConfigurationBase;
import ru.easydonate.easypayments.core.exception.ConfigurationValidationException;
import ru.easydonate.easypayments.core.util.FileBackupMaker;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class TemplateConfigurationBase extends ConfigurationBase {

    public static final String VALUE_PLACEHOLDER = "_value_";

    protected final EasyPayments plugin;
    protected final Map<String, String[]> keyAliases;
    protected FileConfiguration bukkitConfig;

    public TemplateConfigurationBase(@NotNull EasyPayments plugin) {
        this.plugin = plugin;
        this.keyAliases = new HashMap<>();
        this.bukkitConfig = new YamlConfiguration();
    }

    protected abstract @NotNull String getResourcePath();

    protected abstract @NotNull Path getOutputFile();

    @Override
    protected @NotNull FileConfiguration getBukkitConfiguration() {
        return bukkitConfig;
    }

    public void registerKeyAliases(@NotNull String key, String... aliases) {
        if (aliases != null && aliases.length != 0) {
            this.keyAliases.put(key, aliases);
        }
    }

    @Override
    public synchronized boolean reload() throws ConfigurationValidationException {
        String resourcePath = getResourcePath();
        plugin.getDebugLogger().debug("Loading configuration '{0}'...", resourcePath);

        List<String> template = loadTemplate(resourcePath);
        if (template == null)
            return false;

        Map<String, Object> defaults = loadDefaults(resourcePath);
        if (defaults == null)
            return false;

        Path outputFile = getOutputFile();
        boolean outputFileExists = Files.isRegularFile(outputFile);
        Map<String, Object> existing = null;

        if (outputFileExists) {
            try {
                existing = loadConfigurationAsMap(Files.newBufferedReader(outputFile, StandardCharsets.UTF_8));
            } catch (IOException ex) {
                plugin.getLogger().severe(String.format("Couldn't read existing configuration in '%s': %s", outputFile, ex.getMessage()));
                plugin.getDebugLogger().error("Couldn't read existing configuration in '{0}'", outputFile);
                plugin.getDebugLogger().error(ex);
                return false;
            }
        }

        Map<String, Object> overrides = getOverrides();
        boolean hasOverrides = !overrides.isEmpty();

        if (hasOverrides || isConfigurationOutdated(defaults, existing)) {
            if (outputFileExists && !hasOverrides) {
                plugin.getDebugLogger().info("Configuration '{0}' is outdated! Performing update...", resourcePath);

                try {
                    Path backupFilePath = FileBackupMaker.createFileBackup(outputFile);
                    if (backupFilePath != null) {
                        plugin.getDebugLogger().info("Configuration backup has been saved to: {0}", backupFilePath);
                    }
                } catch (IOException ex) {
                    plugin.getLogger().severe("Couldn't create a backup file: " + ex.getMessage());
                    plugin.getDebugLogger().error("Couldn't create a backup file");
                    plugin.getDebugLogger().error(ex);
                    return false;
                }
            }

            Map<String, Object> dataValues = mergeConfigurationData(defaults, existing);
            if (hasOverrides)
                dataValues = mergeConfigurationData(dataValues, overrides);

            try {
                new TemplateWriter(outputFile, template, dataValues, keyAliases).write();
            } catch (IOException ex) {
                plugin.getLogger().severe(String.format("Couldn't save generated configuration as '%s': %s", outputFile, ex.getMessage()));
                plugin.getDebugLogger().error("Couldn't save generated configuration as '{0}'", outputFile);
                plugin.getDebugLogger().error(ex);
                return false;
            }
        }

        try {
            this.bukkitConfig.load(Files.newBufferedReader(outputFile, StandardCharsets.UTF_8));
        } catch (IOException | InvalidConfigurationException ex) {
            plugin.getLogger().severe("Couldn't load configuration: " + ex.getMessage());
            plugin.getDebugLogger().error("Couldn't load configuration");
            plugin.getDebugLogger().error(ex);
            return false;
        }

        resetOverrides();
        validate();
        plugin.getDebugLogger().info("Loaded configuration '{0}'", resourcePath);
        return true;
    }

    private Map<String, Object> loadDefaults(String resourcePath) {
        try (InputStream stream = resolveResource("/assets/defaults/" + resourcePath)) {
            InputStreamReader inputStreamReader = new InputStreamReader(stream, StandardCharsets.UTF_8);
            return loadConfigurationAsMap(inputStreamReader);
        } catch (IOException ex) {
            plugin.getLogger().severe(String.format("Couldn't load defaults resource for configuration '%s': %s", resourcePath, ex.getMessage()));
            plugin.getDebugLogger().error("Couldn't load defaults resource for configuration '{0}'", resourcePath);
            plugin.getDebugLogger().error(ex);
        }
        return null;
    }

    private List<String> loadTemplate(String resourcePath) {
        try (InputStream stream = resolveResource("/assets/templates/" + resourcePath)) {
            InputStreamReader inputStreamReader = new InputStreamReader(stream, StandardCharsets.UTF_8);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            return bufferedReader.lines().collect(Collectors.toCollection(ArrayList::new));
        } catch (IOException ex) {
            plugin.getLogger().severe(String.format("Couldn't load template resource for configuration '%s': %s", resourcePath, ex.getMessage()));
            plugin.getDebugLogger().error("Couldn't load template resource for configuration '{0}'", resourcePath);
            plugin.getDebugLogger().error(ex);
        }
        return null;
    }

    private InputStream resolveResource(String resourcePath) throws IOException {
        URL url = getClass().getResource(resourcePath);
        if (url == null)
            throw new IOException("resource not found");

        return url.openStream();
    }

    private static boolean isConfigurationOutdated(Map<String, Object> defaults, Map<String, Object> existing) {
        if (defaults == null || defaults.isEmpty())
            return false;

        if (existing == null || existing.isEmpty())
            return true;

        if (defaults.size() != existing.size())
            return true;

        for (String defaultsKey : defaults.keySet())
            if (existing.get(defaultsKey) == null)
                return true;

        return false;
    }

    private static Map<String, Object> mergeConfigurationData(Map<String, Object> defaults, Map<String, Object> existing) {
        if (existing == null || existing.isEmpty())
            return defaults;

        Map<String, Object> resultMap = new HashMap<>(defaults);
        existing.forEach((key, value) -> {
            if (value != null && !VALUE_PLACEHOLDER.equals(value)) {
                resultMap.put(key, value);
            }
        });

        return resultMap;
    }

    private static Map<String, Object> loadConfigurationAsMap(Reader reader) {
        YamlConfiguration defaultsConfig = YamlConfiguration.loadConfiguration(reader);
        Map<String, Object> defaultsMap = new HashMap<>(defaultsConfig.getValues(true));
        defaultsMap.entrySet().removeIf(entry -> entry.getValue() instanceof ConfigurationSection);
        return defaultsMap;
    }

}
