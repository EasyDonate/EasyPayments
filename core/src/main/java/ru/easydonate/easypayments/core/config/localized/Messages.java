package ru.easydonate.easypayments.core.config.localized;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.easydonate.easypayments.core.config.Configuration;
import ru.easydonate.easypayments.core.formatting.StringFormatter;

import java.nio.file.Path;
import java.util.function.Consumer;

public final class Messages extends LocalizedConfigurationBase {

    public static final String LANG_KEY = "lang";
    public static final String UNDEFINED_MESSAGE_FORMAT = ChatColor.RED + "Undefined message '%s'";

    public Messages(@NotNull Plugin plugin, @NotNull Configuration config) {
        super(plugin, config);
    }

    @Override
    protected @NotNull String getConfigLangKey() {
        return LANG_KEY;
    }

    @Override
    protected @NotNull String getResourcePath() {
        return String.format("lang/%s.yml", getUsedLocale().getLanguageTag());
    }

    @Override
    protected @NotNull Path getOutputFile() {
        String fileName = String.format("messages_%s.yml", getUsedLocale().getLanguageTag());
        return plugin.getDataFolder().toPath().resolve("lang").resolve(fileName);
    }

    public @NotNull String get(@NotNull String key, @Nullable Object... args) {
        return StringFormatter.format(getColoredString(key, () -> String.format(UNDEFINED_MESSAGE_FORMAT, key)), args);
    }

    public @NotNull String getOrDefault(@NotNull String key, @Nullable String def, @Nullable Object... args) {
        return StringFormatter.format(getColoredString(key, def), args);
    }

    public @NotNull Messages getAndSend(@NotNull CommandSender sender, @NotNull String key, @Nullable Object... args) {
        String message = StringFormatter.format(getColoredString(key), args);
        return send(sender, message);
    }

    public @NotNull Messages getAndSend(@NotNull Consumer<String> sendFunction, @NotNull String key, @Nullable Object... args) {
        String message = StringFormatter.format(getColoredString(key), args);
        sendFunction.accept(message);
        return this;
    }

    public @NotNull Messages send(@NotNull CommandSender sender, @NotNull String message) {
        if(message != null && !message.isEmpty()) {
            if(sender instanceof Player) {
                sender.sendMessage(message);
            } else {
                for(String line : message.split("\n")) {
                    sender.sendMessage(line);
                }
            }
        }

        return this;
    }

}
