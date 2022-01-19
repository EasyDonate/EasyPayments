package ru.easydonate.easypayments.config;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum LocaleEnum {

    ENGLISH("en-US"),
    RUSSIAN("ru-RU");

    private final String languageTag;

    public static @NotNull LocaleEnum getSystemDefault() {
        String languageTag = Locale.getDefault().toLanguageTag();
        LocaleEnum found = getByTag(languageTag);
        return found != null ? found : ENGLISH;
    }

    public static @Nullable LocaleEnum getByTag(@Nullable String languageTag) {
        if(languageTag != null && !languageTag.isEmpty())
            for(LocaleEnum locale : values())
                if(locale.getLanguageTag().equalsIgnoreCase(languageTag))
                    return locale;

        return null;
    }

}
