package ru.easydonate.easypayments.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;
import ru.easydonate.easydonate4j.exception.HttpRequestException;
import ru.easydonate.easydonate4j.exception.HttpResponseException;
import ru.easydonate.easypayments.EasyPaymentsPlugin;
import ru.easydonate.easypayments.core.config.localized.Messages;
import ru.easydonate.easypayments.core.platform.scheduler.PlatformScheduler;
import ru.easydonate.easypayments.database.DatabaseManager;
import ru.easydonate.easypayments.database.model.Customer;
import ru.easydonate.easypayments.database.model.Payment;
import ru.easydonate.easypayments.shopcart.ShopCart;
import ru.easydonate.easypayments.shopcart.ShopCartStorage;

import java.util.Collection;

public final class PlayerJoinQuitListener implements Listener {

    private final EasyPaymentsPlugin plugin;
    private final PlatformScheduler scheduler;

    private final Messages messages;
    private final ShopCartStorage shopCartStorage;

    public PlayerJoinQuitListener(
            @NotNull EasyPaymentsPlugin plugin,
            @NotNull Messages messages,
            @NotNull ShopCartStorage shopCartStorage
    ) {
        this.plugin = plugin;
        this.scheduler = plugin.getPlatformProvider().getScheduler();

        this.messages = messages;
        this.shopCartStorage = shopCartStorage;

        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerJoin(@NotNull PlayerJoinEvent event) {
        if (!EasyPaymentsPlugin.isPluginEnabled())
            return;

        Player player = event.getPlayer();
        scheduler.runAsyncNow(plugin, () -> {
            updateCustomerOwnership(player);
            notifyAboutVersionUpdate(player);
            shopCartStorage.loadAndCache(player)
                    .thenAccept(shopCart -> issueOrNotifyAboutCartContent(player, shopCart))
                    .join();
        });
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerQuit(@NotNull PlayerQuitEvent event) {
        if (!EasyPaymentsPlugin.isPluginEnabled())
            return;

        Player player = event.getPlayer();
        shopCartStorage.unloadCached(player.getName());
    }

    private void issueOrNotifyAboutCartContent(@NotNull Player player, @NotNull ShopCart shopCart) {
        if (plugin.getShopCartConfig().shouldIssueWhenOnline()) {
            Collection<Payment> payments = shopCart.getContent();
            if (payments == null || payments.isEmpty())
                return;

            try {
                plugin.getDebugLogger().info("Issuing purchases to {0}, because auto-issue is enabled...", player.getName());
                plugin.getIssuancePerformService().issuePurchasesAndReport(payments);
            } catch (HttpRequestException | HttpResponseException ex) {
                plugin.getLogger().severe("An unknown error occured while trying to upload reports!");
                plugin.getLogger().severe("Please, contact with the platform support:");
                plugin.getLogger().severe(EasyPaymentsPlugin.SUPPORT_URL);
                plugin.getDebugLogger().error(ex);
            } catch (Throwable ex) {
                plugin.getDebugLogger().error("An unexpected error was occured!");
                plugin.getDebugLogger().error(ex);
            }
        } else {
            notifyAboutCartContent(player, shopCart);
        }
    }

    private void notifyAboutCartContent(@NotNull Player player, @NotNull ShopCart shopCart) {
        if (!player.hasPermission("easypayments.notify.cart"))
            return;

        if (shopCart == null || shopCart.isEmpty())
            return;

        messages.getAndSend(player, "cart-notification");
    }

    private void notifyAboutVersionUpdate(@NotNull Player player) {
        if (!player.hasPermission("easypayments.notify.update"))
            return;

        plugin.getVersionResponse().ifPresent(response -> messages.getAndSend(player, "update-notification",
                "%current_version%", plugin.getDescription().getVersion(),
                "%available_version%", response.getVersion(),
                "%download_url%", response.getDownloadUrl()
        ));
    }

    private void updateCustomerOwnership(@NotNull Player player) {
        DatabaseManager databaseManager = shopCartStorage.getStorage();

        Customer customer = databaseManager.getCustomer(player).join();
        if (customer == null)
            return;

        if (databaseManager.isUuidIdentificationEnabled()) {
            // UUID = constant, updating player name
            if (!player.getName().equals(customer.getPlayerName())) {
                databaseManager.transferCustomerOwnership(customer, player.getName()).join();
            }
        } else {
            // name = constant, updating player UUID
            if (!player.getUniqueId().equals(customer.getPlayerUUID())) {
                customer.updateUUID(player.getUniqueId());
                databaseManager.saveCustomer(customer).join();
            }
        }
    }

}
