package ru.easydonate.easypayments.command.easypayments;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import ru.easydonate.easypayments.EasyPaymentsPlugin;
import ru.easydonate.easypayments.command.CommandExecutor;
import ru.easydonate.easypayments.command.annotation.Command;
import ru.easydonate.easypayments.command.annotation.CommandAliases;
import ru.easydonate.easypayments.command.annotation.Permission;
import ru.easydonate.easypayments.command.exception.ExecutionException;
import ru.easydonate.easypayments.command.exception.InitializationException;
import ru.easydonate.easypayments.config.Messages;

import java.util.List;

@Command("reload")
@CommandAliases({"r", "refresh"})
@Permission("easypayments.command.reload")
public final class CommandReload extends CommandExecutor {

    private final EasyPaymentsPlugin plugin;

    public CommandReload(@NotNull EasyPaymentsPlugin plugin, @NotNull Messages messages) throws InitializationException {
        super(messages);
        this.plugin = plugin;
    }

    @Override
    public void executeCommand(@NotNull CommandSender sender, @NotNull List<String> args) throws ExecutionException {
        validateExecution(sender, args);

        try {
            plugin.reload();
            messages.getAndSend(sender, "reload.success");
        } catch (Exception ex) {
            messages.getAndSend(sender, "reload.failed.some-error-occurred",
                    "%error_message%", ex.getMessage(),
                    "%troubleshooting_page_url%", EasyPaymentsPlugin.TROUBLESHOOTING_PAGE_URL
            );
        }
    }

}
