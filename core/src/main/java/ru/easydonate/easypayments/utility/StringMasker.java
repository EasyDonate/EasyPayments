package ru.easydonate.easypayments.utility;

import org.jetbrains.annotations.Nullable;

public final class StringMasker {

    private static final char MASKING_CHAR = '*';

    public static @Nullable String maskAccessKey(@Nullable String input) {
        if(input == null || input.isEmpty())
            return input;

        char[] chars = input.toCharArray();
        int length = chars.length;

        if(length > 8)
            for(int i = 4; i < length - 4; i++)
                chars[i] = MASKING_CHAR;

        return new String(chars);
    }

}
