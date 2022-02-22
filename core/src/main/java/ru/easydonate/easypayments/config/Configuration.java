package ru.easydonate.easypayments.config;

import lombok.Getter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import ru.easydonate.easypayments.exception.ConfigurationValidationException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Getter
public final class Configuration extends AbstractConfiguration<Configuration> {

    private static final Pattern REPLACEABLE_NODE_REGEX = Pattern.compile("\\$\\$([\\w\\d\\s._-]*)\\$\\$");
    private static final Map<String, Object> DEFAULT_NODE_VALUES = new HashMap<>();
    private static final String DEFAULT_NODE_VALUE = "null";

    static {
        DEFAULT_NODE_VALUES.put("server-id", 0);
        DEFAULT_NODE_VALUES.put("permission-level", 4);
        DEFAULT_NODE_VALUES.put("logging.debug", false);
        DEFAULT_NODE_VALUES.put("logging.query-task-errors", true);
        DEFAULT_NODE_VALUES.put("logging.cache-worker-warnings", true);
        DEFAULT_NODE_VALUES.put("logging.cache-worker-errors", true);
    }

    private final String fileName;
    private final String resourcePath;
    private Validator<Configuration> validator;

    public Configuration(@NotNull Plugin plugin, @NotNull String fileName) {
        this(plugin, fileName, String.format("/%s", fileName));
    }

    public Configuration(@NotNull Plugin plugin, @NotNull String fileName, String resourcePath) {
        super(plugin);
        this.fileName = fileName;
        this.resourcePath = resourcePath;
    }

    @Override
    protected Configuration getThis() {
        return this;
    }

    @Override
    public @NotNull Configuration reload() throws ConfigurationValidationException {
        super.reload();

        if(!isNewFileCreated() && bukkitConfig != null) {
            // 'database' section not exists? it's a deprecated config :(
            if(!bukkitConfig.isConfigurationSection("database")) {
                try {
                    plugin.getLogger().info("Updating config.yml structure...");
                    patchDeprecatedConfig();
                    super.reload();
                } catch (IOException ex) {
                    plugin.getLogger().severe("Couldn't update config.yml structure: " + ex.getMessage());
                    Throwable cause = ex.getCause();
                    while(cause != null) {
                        plugin.getLogger().severe(cause.getMessage());
                        cause = cause.getCause();
                    }
                }
            }
        }

        if(validator != null) {
            validator.validate(this);
        }

        return this;
    }

    public @NotNull Configuration withValidator(@NotNull Validator<Configuration> validator) {
        this.validator = validator;
        return this;
    }

    private void patchDeprecatedConfig() throws IOException {
        InputStream templateResource = getResource("/templates/config.yml", true);
        List<String> templateContent = readContent(templateResource);
        templateContent.replaceAll(this::patchConfigLine);

        Path outputFilePath = resolveOutputFilePath();
        Files.write(outputFilePath, templateContent, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    private @NotNull String patchConfigLine(@NotNull String line) {
        if(line == null || line.isEmpty() || line.startsWith("#") || line.trim().isEmpty())
            return line;

        Matcher matcher = REPLACEABLE_NODE_REGEX.matcher(line);

        StringBuilder builder = new StringBuilder(line);
        while(matcher.find()) {
            int start = matcher.start();
            int end = matcher.end();
            if(start == -1 || end == -1)
                continue;

            int groupStart = matcher.start(1);
            int groupEnd = matcher.end(1);
            if(groupStart == -1 || groupEnd == -1)
                continue;

            String key = builder.substring(groupStart, groupEnd);
            if(key.isEmpty())
                continue;

            String value = resolveNodeValue(key);
            builder.replace(start, end, value);
        }

        return builder.toString();
    }

    private @NotNull String resolveNodeValue(@NotNull String key) {
        Object value = bukkitConfig.get(key);
        if(value != null && !(value instanceof ConfigurationSection))
            return value.toString();

        Object defaultValue = DEFAULT_NODE_VALUES.get(key);
        return defaultValue != null ? defaultValue.toString() : DEFAULT_NODE_VALUE;
    }

    private @NotNull List<String> readContent(@NotNull InputStream inputStream) {
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

        List<String> lines = new ArrayList<>();
        String line;

        try {
            while((line = bufferedReader.readLine()) != null) {
                lines.add(line);
            }
        } catch (IOException ignored) {
        }

        return lines;
    }

}
