package ru.easydonate.easypayments.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitScheduler;
import org.jetbrains.annotations.NotNull;
import ru.easydonate.easypayments.EasyPaymentsPlugin;
import ru.easydonate.easypayments.config.Messages;
import ru.easydonate.easypayments.database.DatabaseManager;
import ru.easydonate.easypayments.database.model.Customer;
import ru.easydonate.easypayments.shopcart.ShopCart;
import ru.easydonate.easypayments.shopcart.ShopCartStorage;

public final class PlayerJoinQuitListener implements Listener {

    private final EasyPaymentsPlugin plugin;
    private final BukkitScheduler scheduler;

    private final Messages messages;
    private final ShopCartStorage shopCartStorage;

    public PlayerJoinQuitListener(
            @NotNull EasyPaymentsPlugin plugin,
            @NotNull Messages messages,
            @NotNull ShopCartStorage shopCartStorage
    ) {
        this.plugin = plugin;
        this.scheduler = plugin.getServer().getScheduler();

        this.messages = messages;
        this.shopCartStorage = shopCartStorage;

        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerJoin(@NotNull PlayerJoinEvent event) {
        if(!EasyPaymentsPlugin.isPluginEnabled())
            return;

        Player player = event.getPlayer();
        scheduler.runTaskAsynchronously(plugin, () -> {
            updateCustomerOwnership(player);
            notifyAboutVersionUpdate(player);
            shopCartStorage.loadAndCache(player)
                    .thenAccept(shopCart -> notifyAboutCartContent(player, shopCart))
                    .join();
        });
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerQuit(@NotNull PlayerQuitEvent event) {
        if(!EasyPaymentsPlugin.isPluginEnabled())
            return;

        Player player = event.getPlayer();
        shopCartStorage.unloadCached(player.getName());
    }

    private void notifyAboutCartContent(@NotNull Player player, @NotNull ShopCart shopCart) {
        if(!player.hasPermission("easypayments.notify.cart"))
            return;

        if(shopCart == null || !shopCart.hasContent())
            return;

        messages.getAndSend(player, "cart-notification");
    }

    private void notifyAboutVersionUpdate(@NotNull Player player) {
        if(!player.hasPermission("easypayments.notify.update"))
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
        if(customer == null)
            return;

        if(databaseManager.isUuidIdentificationEnabled()) {
            // UUID = constant, updating player name
            if(!player.getName().equals(customer.getPlayerName())) {
                databaseManager.transferCustomerOwnership(customer, player.getName()).join();
            }
        } else {
            // name = constant, updating player UUID
            if(!player.getUniqueId().equals(customer.getPlayerUUID())) {
                customer.updateUUID(player.getUniqueId());
                databaseManager.saveCustomer(customer).join();
            }
        }
    }

}
