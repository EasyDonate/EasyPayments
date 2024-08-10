package ru.easydonate.easypayments.core.config.localized;

import org.jetbrains.annotations.NotNull;
import ru.easydonate.easypayments.core.config.Configuration;

public interface LocalizedConfiguration extends Configuration {

    @NotNull LocaleEnum getUsedLocale();

    @NotNull LocaleEnum resolveLocale();

}
