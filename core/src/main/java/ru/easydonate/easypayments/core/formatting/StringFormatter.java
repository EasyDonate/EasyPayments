package ru.easydonate.easypayments.core.formatting;

import lombok.experimental.UtilityClass;
import org.bukkit.ChatColor;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.stream.Collectors;

@UtilityClass
public class StringFormatter {

    private static final char MASKING_CHAR = '*';

    public @Nullable String colorize(@Nullable String input) {
        return input != null && !input.isEmpty()
                ? ChatColor.translateAlternateColorCodes('&', input)
                : input;
    }

    public @Nullable List<String> colorize(@Nullable List<String> input) {
        return input != null && !input.isEmpty()
                ? input.stream().map(StringFormatter::colorize).collect(Collectors.toList())
                : input;
    }

    public @Nullable String maskAccessKey(@Nullable String input) {
        if (input == null || input.isEmpty())
            return input;

        char[] chars = input.toCharArray();
        int length = chars.length;

        if (length > 8)
            for (int i = 4; i < length - 4; i++)
                chars[i] = MASKING_CHAR;

        return new String(chars);
    }

    public @Nullable String format(@Nullable String input, @Nullable Object... args) {
        if (input == null || input.isEmpty())
            return input;

        if (args == null || args.length == 0)
            return input;

        String output = input;
        int length = args.length;

        // 'k1 k2' + [k1, v1, k2, v2] -> 'v1 v2'
        for (int i = 0; i < length; i += 2) {
            if (i == length - 1)
                break;

            Object rawKey = args[i];
            if (rawKey == null)
                continue;

            String key = String.valueOf(rawKey);
            String value = String.valueOf(args[i + 1]);
            output = output.replace(key, value);
        }

        return output;
    }

}
