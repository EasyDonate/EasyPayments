package ru.easydonate.easypayments.command.easypayments;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import ru.easydonate.easypayments.command.CommandDispatcher;
import ru.easydonate.easypayments.command.annotation.Command;
import ru.easydonate.easypayments.command.annotation.CommandAliases;
import ru.easydonate.easypayments.command.annotation.Permission;
import ru.easydonate.easypayments.command.easypayments.setup.CommandAccessKey;
import ru.easydonate.easypayments.command.easypayments.setup.CommandServerId;
import ru.easydonate.easypayments.command.exception.ExecutionException;
import ru.easydonate.easypayments.command.exception.InitializationException;
import ru.easydonate.easypayments.core.config.Configuration;
import ru.easydonate.easypayments.core.config.localized.Messages;
import ru.easydonate.easypayments.exception.UnsupportedCallerException;
import ru.easydonate.easypayments.setup.InteractiveSetupProvider;

@Command("setup")
@CommandAliases({"config", "configure"})
@Permission("easypayments.command.setup")
public final class CommandSetup extends CommandDispatcher {

    private final InteractiveSetupProvider setupProvider;

    public CommandSetup(
            @NotNull Configuration config,
            @NotNull Messages messages,
            @NotNull InteractiveSetupProvider setupProvider
    ) throws InitializationException {
        super(messages);
        this.setupProvider = setupProvider;

        registerChild(new CommandAccessKey(config, messages));
        registerChild(new CommandServerId(config, messages));
    }

    @Override
    protected void onUsageWithoutArgs(@NotNull CommandSender sender) throws ExecutionException {
        validateExecution(sender);

        if(setupProvider.hasSession(sender)) {
            setupProvider.closeSession(sender);
            messages.getAndSend(sender, "setup.exit");
        } else {
            try {
                setupProvider.openSession(sender, true);
            } catch (UnsupportedCallerException ex) {
                messages.getAndSend(sender, "error.unsupported-caller");
            }
        }
    }

}
