package ru.easydonate.easypayments.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.jetbrains.annotations.NotNull;
import ru.easydonate.easypayments.database.DatabaseManager;
import ru.easydonate.easypayments.database.model.Customer;
import ru.easydonate.easypayments.shopcart.ShopCartStorage;

public final class PlayerJoinQuitListener implements Listener {

    private final Plugin plugin;
    private final BukkitScheduler scheduler;

    private final ShopCartStorage shopCartStorage;
    private final DatabaseManager databaseManager;

    public PlayerJoinQuitListener(@NotNull Plugin plugin, @NotNull ShopCartStorage shopCartStorage) {
        this.plugin = plugin;
        this.scheduler = plugin.getServer().getScheduler();

        this.shopCartStorage = shopCartStorage;
        this.databaseManager = shopCartStorage.getDatabaseManager();

        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerJoin(@NotNull PlayerJoinEvent event) {
        Player player = event.getPlayer();
        scheduler.runTaskAsynchronously(plugin, () -> {
            updateCustomerOwnership(player);
            shopCartStorage.loadAndCache(player).join();
        });
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerQuit(@NotNull PlayerQuitEvent event) {
        Player player = event.getPlayer();
        shopCartStorage.unloadCached(player.getName());
    }

    private void updateCustomerOwnership(@NotNull Player player) {
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
