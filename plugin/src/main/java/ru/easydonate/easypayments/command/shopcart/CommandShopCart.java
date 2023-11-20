package ru.easydonate.easypayments.command.shopcart;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import ru.easydonate.easypayments.EasyPaymentsPlugin;
import ru.easydonate.easypayments.command.CommandDispatcher;
import ru.easydonate.easypayments.command.annotation.Command;
import ru.easydonate.easypayments.command.exception.InitializationException;
import ru.easydonate.easypayments.config.Messages;
import ru.easydonate.easypayments.shopcart.ShopCartStorage;

@Command("shopcart")
public final class CommandShopCart extends CommandDispatcher {

    public CommandShopCart(
            @NotNull EasyPaymentsPlugin plugin,
            @NotNull Messages messages,
            @NotNull ShopCartStorage shopCartStorage
    ) throws InitializationException {
        super(messages);

        registerChild(new CommandGet(plugin, messages, shopCartStorage));
        registerChild(new CommandBrowse(plugin, messages, shopCartStorage));
        registerChild(new CommandClear(plugin, messages, shopCartStorage));
        registerChild(new CommandTransfer(plugin, messages, shopCartStorage));

        register(plugin);
    }

    @Override
    protected void onUsageWithoutArgs(@NotNull CommandSender sender) {
        Bukkit.dispatchCommand(sender, "easypayments help");
    }

}
