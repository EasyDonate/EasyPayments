package ru.easydonate.easypayments.command;

import lombok.Getter;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.easydonate.easypayments.EasyPaymentsPlugin;
import ru.easydonate.easypayments.command.annotation.*;
import ru.easydonate.easypayments.command.exception.ExecutionException;
import ru.easydonate.easypayments.command.exception.InitializationException;
import ru.easydonate.easypayments.core.config.localized.Messages;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Getter
public abstract class CommandExecutor implements Executor {

    protected CommandExecutor parent;
    protected final Messages messages;

    private final String command;
    private final String[] aliases;
    private final String[] arguments;

    private final int minimalArgsCount;
    private final boolean onlyForPlayers;
    private final boolean pluginEnableRequired;
    private final String permission;

    protected CommandExecutor(@NotNull Messages messages) throws InitializationException {
        this.messages = messages;

        this.command = resolveCommand();
        this.aliases = resolveCommandAliases();
        this.arguments = resolveArguments();

        this.minimalArgsCount = resolveMinimalArgsCount();
        this.onlyForPlayers = resolveOnlyForPlayersStatus();
        this.pluginEnableRequired = resolvePluginEnableRequiredStatus();
        this.permission = resolvePermission();
    }

    public void setParent(@NotNull CommandExecutor parent) {
        this.parent = parent;
    }

    public void register(@NotNull EasyPaymentsPlugin plugin) {
        PluginCommand pluginCommand = plugin.getCommand(command);
        if(pluginCommand != null) {
            pluginCommand.setExecutor(this);
            pluginCommand.setTabCompleter(this);
        }
    }

    public @NotNull String resolveFullCommand() {
        StringBuilder fullCommand = new StringBuilder();

        if(parent != null)
            fullCommand.append(parent.resolveFullCommand()).append(' ');

        fullCommand.append(command);
        return fullCommand.toString();
    }

    protected void validateExecution(@NotNull CommandSender sender) throws ExecutionException {
        validateExecution(sender, Collections.emptyList());
    }

    protected void validateExecution(@NotNull CommandSender sender, @NotNull List<String> args) throws ExecutionException {
        checkFeatureAvailability();
        checkPermission(sender);
        checkCommandSender(sender);
        checkCommandSyntax(sender, args);
    }

    protected void checkPermission(@NotNull CommandSender sender) throws ExecutionException {
        if(permission != null && !sender.hasPermission(permission))
            throwNoPermissions();
    }

    protected void throwNoPermissions() throws ExecutionException {
        throw new ExecutionException(messages.get("error.no-permissions"));
    }

    protected void checkCommandSender(@NotNull CommandSender sender) throws ExecutionException {
        if(onlyForPlayers && !isPlayer(sender))
            throwOnlyForPlayers();
    }

    protected void throwOnlyForPlayers() throws ExecutionException {
        throw new ExecutionException(messages.get("error.only-for-players"));
    }

    protected void checkCommandSyntax(@NotNull CommandSender sender, @NotNull List<String> args) throws ExecutionException {
        if(minimalArgsCount > args.size())
            throwWrongSyntax();
    }

    protected void throwWrongSyntax() throws ExecutionException {
        throw new ExecutionException(messages.get("error.wrong-syntax", "%correct_syntax%", resolveCorrectSyntax()));
    }

    protected void checkFeatureAvailability() throws ExecutionException {
        if(pluginEnableRequired && !EasyPaymentsPlugin.isPluginEnabled())
            throwUnavailableFeature();
    }

    protected void throwUnavailableFeature() throws ExecutionException {
        throw new ExecutionException(messages.get("error.unavailable-feature"));
    }

    protected boolean isPlayer(@NotNull CommandSender sender) {
        return sender instanceof Player;
    }

    private @NotNull String resolveCommand() throws InitializationException {
        return getAnnotation(Command.class).map(Command::value).orElseThrow(() -> InitializationException.NO_COMMAND_SPECIFIED);
    }

    private @Nullable String[] resolveCommandAliases() {
        return getAnnotation(CommandAliases.class).map(CommandAliases::value).orElse(null);
    }

    private @Nullable String[] resolveArguments() {
        return getAnnotation(Arguments.class).map(Arguments::value).orElse(null);
    }

    private @NotNull String resolveCorrectSyntax() {
        StringBuilder syntax = new StringBuilder()
                .append('/')
                .append(resolveFullCommand());

        if(arguments != null) {
            for(String key : arguments) {
                String argument = messages.getOrDefault("help.arguments." + key, '<' + key + '>');
                syntax.append(' ').append(argument);
            }
        }

        return syntax.toString();
    }

    private int resolveMinimalArgsCount() {
        return getAnnotation(MinimalArgsCount.class).map(MinimalArgsCount::value).orElse(0);
    }

    private boolean resolveOnlyForPlayersStatus() {
        return getAnnotation(OnlyForPlayers.class).isPresent();
    }

    private boolean resolvePluginEnableRequiredStatus() {
        return getAnnotation(PluginEnableRequired.class).isPresent();
    }

    private @NotNull String resolvePermission() {
        return getAnnotation(Permission.class).map(Permission::value).orElse(null);
    }

    private <T extends Annotation> @NotNull Optional<T> getAnnotation(@NotNull Class<T> annotationType) {
        return Optional.ofNullable(getClass().getAnnotation(annotationType));
    }

}
