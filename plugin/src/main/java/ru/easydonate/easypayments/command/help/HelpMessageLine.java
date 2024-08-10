package ru.easydonate.easypayments.command.help;

import lombok.var;
import org.bukkit.permissions.Permissible;
import org.jetbrains.annotations.NotNull;
import ru.easydonate.easypayments.core.formatting.StringFormatter;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public final class HelpMessageLine {

    private final HelpMessageFactory parentFactory;
    private final List<Supplier<String>> arguments;

    private String command;
    private Supplier<String> description;
    private String permission;

    HelpMessageLine(@NotNull HelpMessageFactory parentFactory) {
        this.parentFactory = parentFactory;
        this.arguments = new ArrayList<>();
    }

    void formatAsMessageLine(@NotNull Permissible receiver, @NotNull String lineFormat, @NotNull List<String> content) {
        if(command == null || !hasPermissionToSee(receiver))
            return;

        StringBuilder commandLine = new StringBuilder(command);
        if (!arguments.isEmpty())
            for (var argument : arguments)
                commandLine.append(' ').append(argument.get());

        content.add(StringFormatter.format(lineFormat,
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

        if (useAsDefaultKey) {
            withDescriptionFrom(command);
            withPermission(command);
        }

        return this;
    }

    public @NotNull HelpMessageLine withArgument(@NotNull String argument) {
        arguments.add(() -> argument);
        return this;
    }

    public @NotNull HelpMessageLine withArguments(@NotNull String... arguments) {
        for(String argument : arguments)
            withArgument(argument);
        return this;
    }

    public @NotNull HelpMessageLine withArgumentFrom(@NotNull String key) {
        Supplier<String> argument = key.startsWith("$")
                ? () -> parentFactory.getMessages().get(key.substring(1))
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
        this.description = () -> description;
        return this;
    }

    public @NotNull HelpMessageLine withDescriptionFrom(@NotNull String key) {
        return withDescriptionFrom(key, true);
    }

    public @NotNull HelpMessageLine withDescriptionFrom(@NotNull String key, boolean useDefaultFormat) {
        this.description = useDefaultFormat ? parentFactory.getDescription(key) : () -> parentFactory.getMessages().get(key);
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

    private @NotNull Supplier<String> getNonNullDescription() {
        return description != null ? description : () -> command;
    }

}
