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
import ru.easydonate.easypayments.config.Messages;
import ru.easydonate.easypayments.database.model.Customer;
import ru.easydonate.easypayments.database.model.Payment;
import ru.easydonate.easypayments.shopcart.ShopCart;
import ru.easydonate.easypayments.shopcart.ShopCartStorage;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;


@Command("transfer")
@Arguments({"source", "payment-id", "target"})
@PluginEnableRequired
@MinimalArgsCount(3)
@Permission("easypayments.command.cart.tranfer")
public final class CommandTransfer extends CommandExecutor {

    private final EasyPaymentsPlugin plugin;
    private final ShopCartStorage shopCartStorage;

    public CommandTransfer(
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

        String sourceName = args.get(0);
        String targetName = args.get(2);

        if (sourceName.isEmpty() || targetName.isEmpty() || sourceName.equals(targetName))
            throwWrongSyntax();

        int paymentId = parsePaymentId(args.get(1));
        if (paymentId == 0) {
            messages.getAndSend(sender, "cart-transfer.failed.bad-payment-id");
            return;
        }

        CompletableFuture<Customer> targetCustomerFuture = shopCartStorage.getStorage().getCustomerByName(targetName);
        shopCartStorage.getStorage().getPayment(paymentId).thenAccept(payment -> {
            if (payment == null) {
                messages.getAndSend(sender, "cart-transfer.failed.payment-not-found", "%payment_id%", paymentId);
                return;
            }

            Customer sourceCustomer = payment.getCustomer();
            if (sourceCustomer == null || !sourceName.equals(sourceCustomer.getPlayerName())) {
                messages.getAndSend(sender, "cart-transfer.failed.payment-not-owned",
                        "%payment_id%", paymentId,
                        "%source%", sourceName
                );
                return;
            }

            if (!payment.hasPurchases()) {
                messages.getAndSend(sender, "cart-transfer.failed.no-purchases", "%payment_id%", paymentId);
                return;
            }

            if (payment.isCollected()) {
                messages.getAndSend(sender, "cart-transfer.failed.purchases-already-collected", "%payment_id%", paymentId);
                return;
            }

            Customer targetCustomer = targetCustomerFuture.join();
            if (targetCustomer == null) {
                messages.getAndSend(sender, "cart-transfer.failed.target-cart-not-found", "%target%", targetName);
                return;
            }

            payment.transfer(targetCustomer);

            shopCartStorage.getStorage().savePayment(payment).thenRun(() -> {
                shopCartStorage.getStorage().refreshCustomer(sourceCustomer);
                shopCartStorage.getStorage().refreshCustomer(targetCustomer);

                messages.getAndSend(sender, "cart-transfer.success",
                        "%source%", sourceName,
                        "%payment_id%", paymentId,
                        "%target%", targetName
                );
            });
        });
    }

    @Override
    public @Nullable List<String> provideTabCompletions(@NotNull CommandSender sender, @NotNull List<String> args) throws ExecutionException {
        validateExecution(sender);

        if (args.size() == 1 || args.size() == 3) {
            String arg = args.get(args.size() - 1).toLowerCase();
            return Arrays.stream(plugin.getServer().getOfflinePlayers())
                    .map(OfflinePlayer::getName)
                    .distinct()
                    .filter(n -> n.toLowerCase().startsWith(arg))
                    .collect(Collectors.toList());
        } else if (args.size() == 2) {
            Optional<ShopCart> cachedCart = shopCartStorage.getCached(args.get(0));
            if (cachedCart.isPresent()) {
                return cachedCart.get().getPayments().stream()
                        .map(Payment::getId)
                        .map(String::valueOf)
                        .collect(Collectors.toList());
            }
        }

        return null;
    }

    private int parsePaymentId(String arg) {
        try {
            return Math.max(0, Integer.parseInt(arg));
        } catch (NumberFormatException ignored) {
            return 0;
        }
    }

}
