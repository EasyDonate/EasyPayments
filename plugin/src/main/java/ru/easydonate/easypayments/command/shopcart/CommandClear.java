package ru.easydonate.easypayments.command.shopcart;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.easydonate.easydonate4j.exception.HttpRequestException;
import ru.easydonate.easydonate4j.exception.HttpResponseException;
import ru.easydonate.easypayments.EasyPaymentsPlugin;
import ru.easydonate.easypayments.command.CommandExecutor;
import ru.easydonate.easypayments.command.annotation.*;
import ru.easydonate.easypayments.command.exception.ExecutionException;
import ru.easydonate.easypayments.command.exception.InitializationException;
import ru.easydonate.easypayments.core.config.localized.Messages;
import ru.easydonate.easypayments.database.model.Payment;
import ru.easydonate.easypayments.core.easydonate4j.extension.data.model.object.NewPaymentReport;
import ru.easydonate.easypayments.shopcart.ShopCart;
import ru.easydonate.easypayments.shopcart.ShopCartStorage;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Command("clear")
@CommandAliases({"delete", "reset"})
@Arguments("player")
@PluginEnableRequired
@Permission("easypayments.command.cart.clear")
public final class CommandClear extends CommandExecutor {

    private static final String CLEAR_OTHERS_PERMISSION = "easypayments.command.cart.clear.other";

    private final EasyPaymentsPlugin plugin;
    private final ShopCartStorage shopCartStorage;

    public CommandClear(
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

        if (!args.isEmpty() && sender.hasPermission(CLEAR_OTHERS_PERMISSION)) {
            clearCart(sender, args.get(0), !isPlayer(sender) || !sender.getName().equals(args.get(0)));
            return;
        }

        if (!isPlayer(sender))
            throwWrongSyntax();

        clearCart(sender, sender.getName(), false);
    }

    @Override
    public @Nullable List<String> provideTabCompletions(@NotNull CommandSender sender, @NotNull List<String> args) throws ExecutionException {
        validateExecution(sender, args);

        if (args.size() != 1)
            return null;

        String arg = args.get(0).toLowerCase();
        if (sender.hasPermission(CLEAR_OTHERS_PERMISSION)) {
            return Arrays.stream(plugin.getServer().getOfflinePlayers())
                    .map(OfflinePlayer::getName)
                    .distinct()
                    .filter(n -> n.toLowerCase().startsWith(arg))
                    .collect(Collectors.toList());
        } else if (isPlayer(sender) && sender.getName().toLowerCase().startsWith(arg)) {
            return Collections.singletonList(sender.getName());
        }

        return null;
    }

    private void clearCart(@NotNull CommandSender sender, @NotNull String playerName, boolean browsingOthers) throws ExecutionException {
        String messagesRoot = browsingOthers ? "cart-clear.other" : "cart-clear.yourself";
        CompletableFuture<ShopCart> shopCartFuture = new CompletableFuture<>();

        Optional<ShopCart> cachedCart = shopCartStorage.getCached(playerName);
        if (!browsingOthers) {
            if (cachedCart.isPresent()) {
                shopCartFuture.complete(cachedCart.get());
            } else {
                plugin.getLogger().warning(String.format("%s's shop cart still isn't cached!", playerName));
                plugin.getLogger().warning("Probably a database connection is very slow...");
                throw new ExecutionException(messages.get(messagesRoot + ".failed.cart-unavailable"));
            }

        } else {
            if (cachedCart.isPresent()) {
                shopCartFuture.complete(cachedCart.get());
            } else {
                shopCartStorage.getAndCache(playerName).thenAccept(shopCartFuture::complete);
            }
        }

        shopCartFuture.thenAccept(shopCart -> {
            if (shopCart == null || shopCart.isEmpty()) {
                messages.getAndSend(sender, messagesRoot + ".failed.no-purchases", "%player%", playerName);
                return;
            }

            List<Payment> collectedPayments = new ArrayList<>();
            CompletableFuture<?>[] futures = shopCart.getPayments().stream()
                    .filter(Payment::markAsCollected)
                    .peek(collectedPayments::add)
                    .map(shopCartStorage.getStorage()::savePayment)
                    .toArray(CompletableFuture<?>[]::new);

            CompletableFuture.allOf(futures).thenAccept(v -> {
                List<NewPaymentReport> reports = collectedPayments.stream()
                        .mapToInt(Payment::getId)
                        .mapToObj(paymentId -> NewPaymentReport.createCartClearReport(paymentId, playerName))
                        .collect(Collectors.toList());

                try {
                    plugin.getExecutionController().uploadCartReports(reports);
                } catch (HttpRequestException | HttpResponseException ex) {
                    plugin.getLogger().severe("An unknown error occured while trying to upload reports!");
                    plugin.getLogger().severe("Please, contact with the platform support:");
                    plugin.getLogger().severe("https://vk.me/easydonateru");
                    ex.printStackTrace();
                }

                messages.getAndSend(sender, messagesRoot + ".success", "%player%", playerName);
            });
        });
    }

    private CompletableFuture<Payment> syncPaymentWithDatabase(Payment payment) {
        return null;
    }

}
