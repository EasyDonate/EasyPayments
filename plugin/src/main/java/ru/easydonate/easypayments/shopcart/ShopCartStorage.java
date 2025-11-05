package ru.easydonate.easypayments.shopcart;

import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;
import ru.easydonate.easypayments.EasyPaymentsPlugin;
import ru.easydonate.easypayments.database.DatabaseManager;
import ru.easydonate.easypayments.exception.PluginUnavailableException;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public final class ShopCartStorage {

    private final EasyPaymentsPlugin plugin;
    private final Map<String, ShopCart> cachedShopCarts;

    public ShopCartStorage(@NotNull EasyPaymentsPlugin plugin) {
        this.plugin = plugin;
        this.cachedShopCarts = new ConcurrentHashMap<>();
    }

    public @NotNull DatabaseManager getStorage() {
        return plugin.getStorage();
    }

    public @NotNull @UnmodifiableView Map<String, ShopCart> getCachedShopCarts() {
        return Collections.unmodifiableMap(cachedShopCarts);
    }

    public @NotNull ShopCart getShopCart(@NotNull OfflinePlayer bukkitPlayer) {
        return getShopCart(bukkitPlayer.getName(), bukkitPlayer.getUniqueId());
    }

    public @NotNull ShopCart getShopCart(@NotNull String playerName, @NotNull UUID playerId) {
        return getCached(playerName).orElseGet(() -> loadAndCache(playerName, playerId).join());
    }

    public @NotNull Optional<ShopCart> getCached(@NotNull String playerName) {
        return Optional.ofNullable(cachedShopCarts.get(playerName));
    }

    public @NotNull CompletableFuture<ShopCart> getAndCache(@NotNull String playerName) throws PluginUnavailableException {
        return plugin.getStorage().getCustomerByName(playerName).thenApply(customer -> {
            if (customer == null)
                return null;

            ShopCart shopCart = new ShopCart(customer);
            cachedShopCarts.put(playerName, shopCart);
            return shopCart;
        });
    }

    public @NotNull CompletableFuture<ShopCart> loadAndCache(@NotNull OfflinePlayer bukkitPlayer) throws PluginUnavailableException {
        return loadAndCache(bukkitPlayer.getName(), bukkitPlayer.getUniqueId());
    }

    public @NotNull CompletableFuture<ShopCart> loadAndCache(@NotNull String playerName, @NotNull UUID playerId) throws PluginUnavailableException {
        return plugin.getStorage().getOrCreateCustomer(playerName, playerId).thenApply(customer -> {
            ShopCart shopCart = new ShopCart(customer);
            cachedShopCarts.put(playerName, shopCart);
            return shopCart;
        });
    }

    public void unloadCached(@NotNull String playerName) {
        cachedShopCarts.remove(playerName);
    }

}
