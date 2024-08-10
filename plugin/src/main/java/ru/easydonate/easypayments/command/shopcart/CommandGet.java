package ru.easydonate.easypayments.command.shopcart;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import ru.easydonate.easydonate4j.exception.HttpRequestException;
import ru.easydonate.easydonate4j.exception.HttpResponseException;
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Command("get")
@CommandAliases({"all", "collect"})
@OnlyForPlayers
@PluginEnableRequired
@Permission("easypayments.command.cart.get")
public final class CommandGet extends CommandExecutor {

    private final EasyPaymentsPlugin plugin;
    private final ShopCartStorage shopCartStorage;

    public CommandGet(
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

        Player player = (Player) sender;
        Optional<ShopCart> cachedCart = shopCartStorage.getCached(player.getName());

        if(!cachedCart.isPresent()) {
            plugin.getLogger().warning(String.format("%s's shop cart still isn't cached!", player.getName()));
            plugin.getLogger().warning("Probably a database connection is very slow...");
            throw new ExecutionException(messages.get("cart-get.failed.cart-unavailable"));
        }

        ShopCart shopCart = cachedCart.get();
        Collection<Payment> cartContent = shopCart.getContent();

        if(cartContent.isEmpty())
            throw new ExecutionException(messages.get("cart-get.failed.no-purchases"));

        uploadReports(cartContent).thenRun(() -> {
            List<String> purchases = cartContent.stream()
                    .filter(Payment::hasPurchases)
                    .map(Payment::getPurchases)
                    .flatMap(Collection::stream)
                    .map(this::asBodyElement)
                    .collect(Collectors.toList());

            List<String> message = new ArrayList<>();
            message.add(messages.get("cart-get.header"));
            message.addAll(purchases);
            message.add(messages.get("cart-get.footer"));
            message.removeIf(String::isEmpty);

            messages.send(sender, String.join("\n", message));
        });
    }

    private @NotNull String asBodyElement(@NotNull Purchase purchase) {
        String name = purchase.getName();
        int amount = purchase.getAmount();
        LocalDateTime createdAt = purchase.getCreatedAt();

        return messages.get("cart-get.body",
                "%name%", name != null ? name : getNoValueStub(),
                "%amount%", Math.max(amount, 1),
                "%time_ago%", plugin.getRelativeTimeFormatter().formatElapsedTime(createdAt)
        );
    }

    private @NotNull String getNoValueStub() {
        return messages.get("cart-get.no-value-stub");
    }

    private @NotNull CompletableFuture<Void> uploadReports(@NotNull Collection<Payment> payments) {
        return CompletableFuture.runAsync(() -> {
            try {
                plugin.getExecutionController().givePurchasesFromCartAndReport(payments);
            } catch (HttpRequestException | HttpResponseException ex) {
                plugin.getLogger().severe("An unknown error occured while trying to upload reports!");
                plugin.getLogger().severe("Please, contact with the platform support:");
                plugin.getLogger().severe("https://vk.me/easydonateru");
                ex.printStackTrace();
            }
        });
    }

}
