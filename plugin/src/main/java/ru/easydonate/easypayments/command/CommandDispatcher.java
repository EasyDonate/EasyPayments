package ru.easydonate.easypayments.command;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.easydonate.easypayments.command.exception.ExecutionException;
import ru.easydonate.easypayments.command.exception.InitializationException;
import ru.easydonate.easypayments.config.Messages;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class CommandDispatcher extends CommandExecutor {

    private final Map<String, Executor> registeredCommands;

    public CommandDispatcher(@NotNull Messages messages) throws InitializationException {
        super(messages);

        this.registeredCommands = new LinkedHashMap<>();
    }

    protected void onUsageWithoutArgs(@NotNull CommandSender sender) throws ExecutionException {}

    protected void registerChild(@NotNull Executor executor) {
        if(executor instanceof CommandExecutor)
            ((CommandExecutor) executor).setParent(this);

        String command = executor.getCommand();
        registeredCommands.put(command, executor);

        String[] aliases = executor.getAliases();
        if(aliases != null && aliases.length != 0)
            for(String alias : aliases)
                registeredCommands.put(alias, executor);
    }

    @Override
    public void executeCommand(@NotNull CommandSender sender, @NotNull List<String> args) throws ExecutionException {
        if(args.isEmpty()) {
            onUsageWithoutArgs(sender);
            return;
        }

        String command = args.remove(0);
        Executor executor = findRelevantExecutor(command);
        if(executor == null)
            throw new ExecutionException(messages.get("error.unknown-command"));

        executor.executeCommand(sender, args);
    }

    @Override
    public @Nullable List<String> provideTabCompletions(@NotNull CommandSender sender, @NotNull List<String> args) throws ExecutionException {
        if(args.isEmpty())
            return null;

        if(args.size() == 1) {
            String input = args.get(0).toLowerCase();
            return registeredCommands.keySet().stream()
                    .filter(command -> command.toLowerCase().startsWith(input))
                    .collect(Collectors.toList());
        }

        String command = args.remove(0);
        Executor executor = findRelevantExecutor(command);
        return executor != null ? executor.provideTabCompletions(sender, args) : null;
    }

    private @Nullable Executor findRelevantExecutor(@Nullable String command) {
        if(command != null && !command.isEmpty())
            for(String registeredCommand : registeredCommands.keySet())
                if(command.equalsIgnoreCase(registeredCommand))
                    return registeredCommands.get(registeredCommand);

        return null;
    }

}
