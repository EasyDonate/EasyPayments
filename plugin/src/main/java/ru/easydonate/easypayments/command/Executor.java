package ru.easydonate.easypayments.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.easydonate.easypayments.command.exception.ExecutionException;
import ru.easydonate.easypayments.core.config.localized.Messages;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public interface Executor extends CommandExecutor, TabCompleter {

    @NotNull String getCommand();

    @Nullable String[] getAliases();

    @NotNull Messages getMessages();

    void executeCommand(@NotNull CommandSender sender, @NotNull List<String> args) throws ExecutionException;

    default @Nullable List<String> provideTabCompletions(@NotNull CommandSender sender, @NotNull List<String> args) throws ExecutionException {
        return null;
    }

    // --- proxied methods from parent interfaces
    @Override
    default boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        try {
            executeCommand(sender, new ArrayList<>(Arrays.asList(args)));
        } catch (ExecutionException ex) {
            // use custom method to correctly send multiline messages
            getMessages().send(sender, ex.getMessage());
        }

        return true;
    }

    @Override
    default @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        try {
            return provideTabCompletions(sender, new ArrayList<>(Arrays.asList(args)));
        } catch (ExecutionException ignored) {
            return null;
        }
    }

}
