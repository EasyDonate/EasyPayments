package ru.easydonate.easypayments.command.easypayments;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import ru.easydonate.easypayments.command.CommandExecutor;
import ru.easydonate.easypayments.command.annotation.CommandAliases;
import ru.easydonate.easypayments.command.annotation.Command;
import ru.easydonate.easypayments.command.annotation.Permission;
import ru.easydonate.easypayments.command.exception.ExecutionException;
import ru.easydonate.easypayments.command.exception.InitializationException;
import ru.easydonate.easypayments.command.help.HelpMessage;
import ru.easydonate.easypayments.command.help.HelpMessageFactory;
import ru.easydonate.easypayments.config.Messages;

import java.util.List;

@Command("help")
@CommandAliases({"h", "?"})
@Permission("easypayments.command.help")
public final class CommandHelp extends CommandExecutor {

    private final HelpMessage helpMessage;

    public CommandHelp(@NotNull Messages messages) throws InitializationException {
        super(messages);

        this.helpMessage = HelpMessageFactory.newFactory(messages)
                .withArgumentKeyFormat("help.arguments.%s")
                .withDescriptionKeyFormat("help.descriptions.%s")
                .withPermissionFormat("easypayments.command.%s")

                .withHeaderFrom("help.header")
                .withLineFormatFrom("help.body")
                .withFooterFrom("help.footer")

                .newLine()
                .withCommand("easypayments help")
                .withDescriptionFrom("help")
                .withPermission("help")
                .add()

                .newLine()
                .withCommand("cart get")
                .withArgumentFrom("player")
                .withDescriptionFrom("cart-get")
                .withPermission("cart.get")
                .add()

                .newLine()
                .withCommand("cart browse")
                .withDescriptionFrom("cart-browse")
                .withPermission("cart.browse")
                .add()

                .newLine()
                .withCommand("cart clear")
                .withDescriptionFrom("cart-clear")
                .withPermission("cart.clear")
                .add()

                .newLine()
                .withCommand("cart transfer")
                .withArgumentFrom("source")
                .withArgumentFrom("payment-id")
                .withArgumentFrom("target")
                .withDescriptionFrom("cart-transfer")
                .withPermission("cart.transfer")
                .add()

                .newLine()
                .withCommand("easypayments status")
                .withDescriptionFrom("status")
                .withPermission("status")
                .add()

                .newLine()
                .withCommand("easypayments setup")
                .withDescriptionFrom("setup")
                .withPermission("setup")
                .add()

                .newLine()
                .withCommand("easypayments setup access-key")
                .withArgumentFrom("access-key")
                .withDescriptionFrom("setup-access-key")
                .withPermission("setup")
                .add()

                .newLine()
                .withCommand("easypayments setup server-id")
                .withArgumentFrom("server-id")
                .withDescriptionFrom("setup-server-id")
                .withPermission("setup")
                .add()

                .newLine()
                .withCommand("easypayments migrate")
                .withArgumentFrom("database-type")
                .withDescriptionFrom("migrate")
                .withPermission("migrate")
                .add()

                .newLine()
                .withCommand("easypayments reload")
                .withDescriptionFrom("reload")
                .withPermission("reload")
                .add()

                .constructMessage();
    }

    @Override
    public void executeCommand(@NotNull CommandSender sender, @NotNull List<String> args) throws ExecutionException {
        validateExecution(sender, args);

        helpMessage.sendTo(sender);
    }

}
