package ru.easydonate.easypayments.command.easypayments.setup;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import ru.easydonate.easypayments.EasyPaymentsPlugin;
import ru.easydonate.easypayments.command.CommandExecutor;
import ru.easydonate.easypayments.command.annotation.*;
import ru.easydonate.easypayments.command.exception.ExecutionException;
import ru.easydonate.easypayments.command.exception.InitializationException;
import ru.easydonate.easypayments.config.Configuration;
import ru.easydonate.easypayments.config.Messages;

import java.util.List;

@Command("server-id")
@CommandAliases({"server", "sid"})
@Arguments("server-id")
@MinimalArgsCount(1)
@Permission("easypayments.command.setup")
public final class CommandServerId extends CommandExecutor {

    private final Configuration config;

    public CommandServerId(@NotNull Configuration config, @NotNull Messages messages) throws InitializationException {
        super(messages);
        this.config = config;
    }

    @Override
    public void executeCommand(@NotNull CommandSender sender, @NotNull List<String> args) throws ExecutionException {
        validateExecution(sender, args);

        try {
            int serverId = Integer.parseInt(args.get(0));
            if(serverId > 0) {
                config.updateExistingFile(EasyPaymentsPlugin.CONFIG_SERVER_ID_REGEX, serverId);
                messages.getAndSend(sender, "setup.success.server-id", "%server_id%", serverId);

                if(config.getString("key", "").isEmpty())
                    messages.getAndSend(sender, "setup.tips.access-key");

                return;
            }
        } catch (NumberFormatException ignored) {
        }

        throw new ExecutionException(messages.get("setup.failed.wrong-server-id"));
    }

}
