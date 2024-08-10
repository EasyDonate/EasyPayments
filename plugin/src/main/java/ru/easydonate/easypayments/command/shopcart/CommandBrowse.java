package ru.easydonate.easypayments.command.shopcart;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.easydonate.easypayments.EasyPaymentsPlugin;
import ru.easydonate.easypayments.command.CommandExecutor;
import ru.easydonate.easypayments.command.annotation.*;
import ru.easydonate.easypayments.command.exception.ExecutionException;
import ru.easydonate.easypayments.command.exception.InitializationException;
import ru.easydonate.easypayments.core.config.localized.Messages;
import ru.easydonate.easypayments.database.model.Payment;
import ru.easydonate.easypayments.database.model.Purchase;
import ru.easydonate.easypayments.shopcart.ShopCart;
import ru.easydonate.easypayments.shopcart.ShopCartStorage;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Command("browse")
@CommandAliases({"check", "list", "content"})
@Arguments("player")
@PluginEnableRequired
@Permission("easypayments.command.cart.browse")
public final class CommandBrowse extends CommandExecutor {

    private static final String BROWSE_OTHERS_PERMISSION = "easypayments.command.cart.browse.other";

    private final EasyPaymentsPlugin plugin;
    private final ShopCartStorage shopCartStorage;

    public CommandBrowse(
            @NotNull EasyPaymentsPlugin plugin,
            @NotNull Messages messages,
            @NotNull ShopCartStorage shopCartStorage
    ) throws InitializationException {
        super(messages);

        this.plugin = plugin;
        this.shopCartStorage = shopCartStorage;
    }

    @Override
    public void executeCommand(@NotNull CommandSender sender, @NotNull List<String> args) throws ExecutionException {
        validateExecution(sender, args);

        if(!args.isEmpty() && sender.hasPermission(BROWSE_OTHERS_PERMISSION)) {
            showCartContent(sender, args.get(0), !isPlayer(sender) || !sender.getName().equals(args.get(0)));
            return;
        }

        if(!isPlayer(sender))
            throwWrongSyntax();

        showCartContent(sender, sender.getName(), false);
    }

    @Override
    public @Nullable List<String> provideTabCompletions(@NotNull CommandSender sender, @NotNull List<String> args) throws ExecutionException {
        validateExecution(sender, args);

        if(args.size() != 1)
            return null;

        String arg = args.get(0).toLowerCase();
        if(sender.hasPermission(BROWSE_OTHERS_PERMISSION)) {
            return Arrays.stream(plugin.getServer().getOfflinePlayers())
                    .map(OfflinePlayer::getName)
                    .distinct()
                    .filter(n -> n.toLowerCase().startsWith(arg))
                    .collect(Collectors.toList());
        } else if(isPlayer(sender) && sender.getName().toLowerCase().startsWith(arg)) {
            return Collections.singletonList(sender.getName());
        }

        return null;
    }

    private void showCartContent(@NotNull CommandSender sender, @NotNull String playerName, boolean browsingOthers) throws ExecutionException {
        String messagesRoot = browsingOthers ? "cart-browse.other" : "cart-browse.yourself";
        CompletableFuture<ShopCart> shopCartFuture = new CompletableFuture<>();

        Optional<ShopCart> cachedCart = shopCartStorage.getCached(playerName);
        if(!browsingOthers) {
            if(cachedCart.isPresent()) {
                shopCartFuture.complete(cachedCart.get());
            } else {
                plugin.getLogger().warning(String.format("%s's shop cart still isn't cached!", playerName));
                plugin.getLogger().warning("Probably a database connection is very slow...");
                throw new ExecutionException(messages.get(messagesRoot + ".failed.cart-unavailable"));
            }

        } else {
            if(cachedCart.isPresent()) {
                shopCartFuture.complete(cachedCart.get());
            } else {
                shopCartStorage.getAndCache(playerName).thenAccept(shopCartFuture::complete);
            }
        }

        shopCartFuture.thenAccept(shopCart -> {
            Collection<Payment> cartContent = shopCart != null ? shopCart.getContent() : null;
            if(cartContent == null || cartContent.isEmpty()) {
                messages.getAndSend(sender, messagesRoot + ".failed.no-purchases", "%player%", playerName);
                return;
            }

            List<String> purchases = cartContent.stream()
                    .filter(Payment::hasPurchases)
                    .map(Payment::getPurchases)
                    .flatMap(Collection::stream)
                    .map(purchase -> asBodyElement(purchase, messagesRoot))
                    .collect(Collectors.toList());

            List<String> message = new ArrayList<>();
            message.add(messages.get(messagesRoot + ".header", "%player%", playerName));
            message.addAll(purchases);
            message.add(messages.get(messagesRoot + ".footer", "%player%", playerName));
            message.removeIf(String::isEmpty);

            messages.send(sender, String.join("\n", message));
        });
    }

    private @NotNull String asBodyElement(@NotNull Purchase purchase, @NotNull String messagesRoot) {
        String name = purchase.getName();
        int amount = purchase.getAmount();
        LocalDateTime createdAt = purchase.getCreatedAt();

        return messages.get(messagesRoot + ".body",
                "%name%", name != null ? name : getNoValueStub(messagesRoot),
                "%amount%", Math.max(amount, 1),
                "%time_ago%", plugin.getRelativeTimeFormatter().formatElapsedTime(createdAt)
        );
    }

    private @NotNull String getNoValueStub(@NotNull String messagesRoot) {
        return messages.get(messagesRoot + ".no-value-stub");
    }

}
