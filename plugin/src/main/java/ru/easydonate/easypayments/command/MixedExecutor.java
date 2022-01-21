package ru.easydonate.easypayments.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

public interface MixedExecutor extends CommandExecutor, TabCompleter {

    void executeCommand(@NotNull CommandSender sender, @NotNull List<String> args);

    default @Nullable List<String> provideTabCompletions(@NotNull CommandSender sender, @NotNull List<String> args) {
        return null;
    }

    // --- proxied methods from parent interfaces
    @Override
    default boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        executeCommand(sender, Arrays.asList(args));
        return true;
    }

    @Override
    default @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        return provideTabCompletions(sender, Arrays.asList(args));
    }

}
