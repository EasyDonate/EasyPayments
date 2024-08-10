package ru.easydonate.easypayments.command.easypayments;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import ru.easydonate.easypayments.EasyPaymentsPlugin;
import ru.easydonate.easypayments.command.CommandDispatcher;
import ru.easydonate.easypayments.command.annotation.Command;
import ru.easydonate.easypayments.command.exception.ExecutionException;
import ru.easydonate.easypayments.command.exception.InitializationException;
import ru.easydonate.easypayments.core.config.Configuration;
import ru.easydonate.easypayments.core.config.localized.Messages;
import ru.easydonate.easypayments.setup.InteractiveSetupProvider;

import java.util.ArrayList;
import java.util.Collections;

@Command("easypayments")
public final class CommandEasyPayments extends CommandDispatcher {

    public CommandEasyPayments(
            @NotNull EasyPaymentsPlugin plugin,
            @NotNull Configuration config,
            @NotNull Messages messages,
            @NotNull InteractiveSetupProvider setupProvider
    ) throws InitializationException {
        super(messages);

        registerChild(new CommandHelp(messages));
        registerChild(new CommandSetup(config, messages, setupProvider));
        registerChild(new CommandStatus(plugin, messages));
        registerChild(new CommandMigrate(plugin, config, messages));
        registerChild(new CommandReload(plugin, messages));

        register(plugin);
    }

    @Override
    protected void onUsageWithoutArgs(@NotNull CommandSender sender) throws ExecutionException {
        executeCommand(sender, new ArrayList<>(Collections.singletonList("help")));
    }

}
