package ru.easydonate.easypayments.core.config.template;

import lombok.AllArgsConstructor;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@AllArgsConstructor
public final class TemplateWriter {

    private static final Pattern CONFIG_LINE_REGEX = Pattern.compile("^(?<offset> +)?(?<key>[\\w-]+):\\s?(?<value>_value_)?");

    private final Path outputFile;
    private final List<String> template;
    private final Map<String, Object> data;
    private final Map<String, String[]> keyAliases;

    public void write() throws IOException {
        if (!Files.isDirectory(outputFile.getParent()))
            Files.createDirectories(outputFile.getParent());

        List<String> content = processTemplate();
        Files.write(outputFile, content, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    private List<String> processTemplate() {
        List<String> keyPath = new ArrayList<>();
        for (int i = 0; i < template.size(); i++) {
            String line = template.get(i);
            if (line.isEmpty() || line.trim().startsWith("#"))
                continue;

            Matcher matcher = CONFIG_LINE_REGEX.matcher(line);
            if (!matcher.find())
                continue;

            String key = matcher.group("key");
            String rawOffset = matcher.group("offset");
            int offset = toDigitOffset(rawOffset);
            int level = offset / 2;

            if (matcher.group("value") != null) {
                String fullKey = toConfigKey(keyPath, key, level);
                Object value = resolveConfigValue(fullKey);
                int valueStartAt = matcher.start("value");
                int valueEndAt = matcher.end("value");
                i += insertConfigValue(i, valueStartAt, valueEndAt, rawOffset, key, value);
            } else {
                if (keyPath.size() > level) {
                    keyPath.set(level, key);
                } else {
                    keyPath.add(key);
                }
            }
        }

        return template;
    }

    private int insertConfigValue(int lineIndex, int start, int end, String offset, String key, Object value) {
        if (value instanceof String) {
            String asString = (String) value;
            if (asString.contains("\n")) {
                boolean nextLineIsEmpty = false;
                if (lineIndex < template.size() - 1) {
                    String nextLine = template.get(lineIndex + 1);
                    nextLineIsEmpty = nextLine.trim().isEmpty();
                }

                insertConfigValueInline(lineIndex, start, end, nextLineIsEmpty ? "|-" : "|");
                String[] lines = asString.split("\n");
                int insertStart = lineIndex + 1;
                String linePrefix = (offset != null ? offset : "") + "  ";

                for (int i = 0, count = lines.length; i < count; i++) {
                    template.add(insertStart + i, linePrefix + lines[i]);
                }

                return lines.length;
            } else {
                insertConfigStringValue(lineIndex, start, end, asString);
            }
        } else if (value instanceof List) {
            List<?> asList = (List<?>) value;
            if (asList.isEmpty()) {
                insertConfigValueInline(lineIndex, start, end, "[]");
            } else {
                template.set(lineIndex, offset + key + ':');
                int insertStart = lineIndex + 1;

                String listItemLine = offset + "- _value_";
                int listItemValueStart = offset.length() + 2;
                int listItemValueEnd = listItemValueStart + 7;

                for (int i = 0, count = asList.size(); i < count; i++) {
                    int index = insertStart + i;
                    template.add(index, listItemLine);
                    insertConfigStringValue(index, listItemValueStart, listItemValueEnd, asList.get(i));
                }

                return asList.size();
            }
        } else {
            insertConfigValueInline(lineIndex, start, end, value != null ? value : "");
            return 0;
        }

        return 0;
    }

    private void insertConfigStringValue(int lineIndex, int start, int end, Object value) {
        String asString = String.valueOf(value);
        boolean isOneWord = asString.matches("\\w+");
        boolean containsApostrophe = asString.contains("'");
        if (containsApostrophe) {
            asString = asString.replace("\"", "\\\"");
            insertConfigValueInline(lineIndex, start, end, '"' + asString + '"');
        } else if (isOneWord) {
            insertConfigValueInline(lineIndex, start, end, asString);
        } else {
            insertConfigValueInline(lineIndex, start, end, '\'' + asString + '\'');
        }
    }

    private void insertConfigValueInline(int lineIndex, int start, int end, Object value) {
        String line = template.get(lineIndex);
        String replaced = new StringBuilder(line).replace(start, end, String.valueOf(value)).toString();
        template.set(lineIndex, replaced);
    }

    private Object resolveConfigValue(String key) {
        if (keyAliases != null && !keyAliases.isEmpty()) {
            String[] aliases = keyAliases.get(key);
            if (aliases != null) {
                for (String alias : aliases) {
                    Object value = data.get(alias);
                    if (value != null) {
                        return value;
                    }
                }
            }
        }

        Object value = data.get(key);
        return value == null ? "" : value;
    }

    private int toDigitOffset(String offset) {
        return offset != null ? offset.length() : 0;
    }

    private String toConfigKey(List<String> keyPath, String key, int level) {
        if (level == 0 || keyPath.isEmpty())
            return key;

        if (keyPath.size() >= level)
            keyPath = keyPath.subList(0, level);

        return String.join(".", keyPath) + '.' + key;
    }

}
