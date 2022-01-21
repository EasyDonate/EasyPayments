package ru.easydonate.easypayments.command.easypayments;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import ru.easydonate.easypayments.command.MixedExecutor;
import ru.easydonate.easypayments.command.help.HelpMessage;
import ru.easydonate.easypayments.command.help.HelpMessageFactory;
import ru.easydonate.easypayments.config.Messages;

import java.util.List;

public final class CommandHelp implements MixedExecutor {

    private final HelpMessage helpMessage;

    public CommandHelp(@NotNull Messages messages) {
        this.helpMessage = HelpMessageFactory.newFactory(messages)
                .withArgumentKeyFormat("help.arguments.%s")
                .withDescriptionKeyFormat("help.descriptions.%s")
                .withPermissionFormat("easypayments.command.%s")
                .withHeaderFrom("help.header")
                .withLineFormatFrom("help.body")
                .withFooterFrom("help.footer")

                .newLine().withCommand("easypayments help").withDescriptionFrom("help").withPermission("help").add()
                .newLine().withCommand("cart get").withDescriptionFrom("cart-get").withPermission("cart.get").add()
                .newLine().withCommand("cart browse").withDescriptionFrom("cart-browse").withPermission("cart.browse").add()
                .newLine().withCommand("easypayments status").withDescriptionFrom("status").withPermission("status").add()
                .newLine().withCommand("easypayments reload").withDescriptionFrom("reload").withPermission("reload").add()

                .constructMessage();
    }

    @Override
    public void executeCommand(@NotNull CommandSender sender, @NotNull List<String> args) {
        helpMessage.sendTo(sender);
    }

}
