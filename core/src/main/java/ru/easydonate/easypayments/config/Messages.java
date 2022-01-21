package ru.easydonate.easypayments.config;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class Messages extends AbstractConfiguration<Messages> {

    public static final String LANG_KEY = "lang";
    public static final String LANG_FILE_NAME_FORMAT = "lang/messages_%s.yml";
    public static final String LANG_RESOURCE_FORMAT = "/lang/messages_%s.yml";
    public static final String UNDEFINED_MESSAGE_STUB = ChatColor.RED + "Undefined message '%s'";

    private final Configuration config;
    private LocaleEnum usedLocale;

    public Messages(@NotNull Plugin plugin, Configuration config) {
        super(plugin);
        this.config = config;
    }

    @Override
    protected @NotNull Messages getThis() {
        return this;
    }

    public @NotNull LocaleEnum getUsedLocale() {
        String langValue = config.getString(LANG_KEY);
        if(langValue != null) {
            LocaleEnum locale = LocaleEnum.getByTag(langValue);
            if(locale != null) {
                return locale;
            }
        }

        plugin.getLogger().severe("Bad locale tag is specified in the config, switching back to system default...");
        return LocaleEnum.getSystemDefault();
    }

    @Override
    public @NotNull String getFileName() {
        return String.format(LANG_FILE_NAME_FORMAT, usedLocale.getLanguageTag());
    }

    @Override
    public @NotNull String getResourcePath() {
        return String.format(LANG_RESOURCE_FORMAT, usedLocale.getLanguageTag());
    }

    @Override
    public @NotNull Messages reload() {
        this.usedLocale = getUsedLocale();
        return super.reload();
    }

    public @NotNull String get(@NotNull String key, @Nullable Object... args) {
        return format(getColoredString(key, () -> String.format(UNDEFINED_MESSAGE_STUB, key)), args);
    }

    public @NotNull String getOrDefault(@NotNull String key, @Nullable String def, @Nullable Object... args) {
        return format(getColoredString(key, def), args);
    }

    public @NotNull Messages send(@NotNull CommandSender sender, @NotNull String key, @Nullable Object... args) {
        String message = format(getColoredString(key), args);
        if(message != null && !message.isEmpty())
            sender.sendMessage(message);
        return this;
    }

}
