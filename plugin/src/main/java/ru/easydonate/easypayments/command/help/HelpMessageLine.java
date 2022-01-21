package ru.easydonate.easypayments.command.help;

import org.bukkit.permissions.Permissible;
import org.jetbrains.annotations.NotNull;
import ru.easydonate.easypayments.config.AbstractConfiguration;
import ru.easydonate.easypayments.utility.StringSupplier;

import java.util.ArrayList;
import java.util.List;

public final class HelpMessageLine {

    private final HelpMessageFactory parentFactory;
    private final List<StringSupplier> arguments;

    private String command;
    private StringSupplier description;
    private String permission;

    HelpMessageLine(@NotNull HelpMessageFactory parentFactory) {
        this.parentFactory = parentFactory;
        this.arguments = new ArrayList<>();
    }

    void formatAsMessageLine(@NotNull Permissible receiver, @NotNull String lineFormat, @NotNull List<String> content) {
        if(command == null || !hasPermissionToSee(receiver))
            return;

        StringBuilder commandLine = new StringBuilder(command);
        if(!arguments.isEmpty())
            for(StringSupplier argument : arguments)
                commandLine.append(' ').append(argument.get());

        content.add(AbstractConfiguration.format(lineFormat,
                "%command%", commandLine.toString(),
                "%description%", getNonNullDescription().get()
        ));
    }

    public @NotNull HelpMessageFactory add() {
        return parentFactory.addLine(this);
    }

    public @NotNull HelpMessageLine withCommand(@NotNull String command) {
        return withCommand(command, true);
    }

    public @NotNull HelpMessageLine withCommand(@NotNull String command, boolean useAsDefaultKey) {
        this.command = command;

        if(useAsDefaultKey) {
            withDescriptionFrom(command);
            withPermission(command);
        }

        return this;
    }

    public @NotNull HelpMessageLine withArgument(@NotNull String argument) {
        arguments.add(StringSupplier.constant(argument));
        return this;
    }

    public @NotNull HelpMessageLine withArguments(@NotNull String... arguments) {
        for(String argument : arguments)
            withArgument(argument);
        return this;
    }

    public @NotNull HelpMessageLine withArgumentFrom(@NotNull String key) {
        StringSupplier argument = key.startsWith("$")
                ? StringSupplier.messageKey(parentFactory.getMessages(), key.substring(1))
                : parentFactory.getArgument(key);

        arguments.add(argument);
        return this;
    }

    public @NotNull HelpMessageLine withArgumentsFrom(@NotNull String... keys) {
        for(String key : keys)
            withArgumentFrom(key);
        return this;
    }

    public @NotNull HelpMessageLine withDescription(@NotNull String description) {
        this.description = StringSupplier.constant(description);
        return this;
    }

    public @NotNull HelpMessageLine withDescriptionFrom(@NotNull String key) {
        return withDescriptionFrom(key, true);
    }

    public @NotNull HelpMessageLine withDescriptionFrom(@NotNull String key, boolean useDefaultFormat) {
        this.description = useDefaultFormat ? parentFactory.getDescription(key) : StringSupplier.constant(key);
        return this;
    }

    public @NotNull HelpMessageLine withPermission(@NotNull String key) {
        return withPermission(key, true);
    }

    public @NotNull HelpMessageLine withPermission(@NotNull String key, boolean useDefaultFormat) {
        this.permission = useDefaultFormat ? parentFactory.getPermission(key) : key;
        return this;
    }

    public boolean hasPermission() {
        return permission != null && !permission.isEmpty();
    }

    public boolean hasPermissionToSee(@NotNull Permissible permissible) {
        return !hasPermission() || permissible.hasPermission(permission);
    }

    private @NotNull StringSupplier getNonNullDescription() {
        return description != null ? description : StringSupplier.constant(command);
    }

}
