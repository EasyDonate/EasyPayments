package ru.easydonate.easypayments.core.config.localized;

import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import ru.easydonate.easypayments.core.config.Configuration;
import ru.easydonate.easypayments.core.config.template.TemplateConfigurationBase;
import ru.easydonate.easypayments.core.exception.ConfigurationValidationException;

public abstract class LocalizedConfigurationBase extends TemplateConfigurationBase implements LocalizedConfiguration {

    private final Configuration config;
    private LocaleEnum usedLocale;

    public LocalizedConfigurationBase(Plugin plugin, Configuration config) {
        super(plugin);
        this.config = config;
        this.usedLocale = LocaleEnum.getSystemDefault();
    }

    protected abstract @NotNull String getConfigLangKey();

    @Override
    public @NotNull LocaleEnum getUsedLocale() {
        return usedLocale;
    }

    @Override
    public @NotNull LocaleEnum resolveLocale() {
        String langValue = config.getString(getConfigLangKey());
        if (langValue != null) {
            LocaleEnum locale = LocaleEnum.getByTag(langValue);
            if (locale != null) {
                return locale;
            }
        }

        plugin.getLogger().severe("Bad locale tag is specified in the config, switching back to system default...");
        return LocaleEnum.getSystemDefault();
    }

    @Override
    public synchronized boolean reload() throws ConfigurationValidationException {
        this.usedLocale = resolveLocale();
        return super.reload();
    }

}
