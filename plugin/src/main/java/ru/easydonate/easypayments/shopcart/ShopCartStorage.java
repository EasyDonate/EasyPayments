package ru.easydonate.easypayments.shopcart;

import lombok.Getter;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;
import ru.easydonate.easypayments.database.DatabaseManager;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Getter
public final class ShopCartStorage {

    private final DatabaseManager databaseManager;
    private final Map<String, ShopCart> cachedShopCarts;

    public ShopCartStorage(@NotNull DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
        this.cachedShopCarts = new ConcurrentHashMap<>();
    }

    public @NotNull @UnmodifiableView Map<String, ShopCart> getCachedShopCarts() {
        return Collections.unmodifiableMap(cachedShopCarts);
    }

    public @NotNull ShopCart getShopCart(@NotNull OfflinePlayer bukkitPlayer) {
        return getCached(bukkitPlayer.getName()).orElseGet(() -> loadAndCache(bukkitPlayer).join());
    }

    public @NotNull Optional<ShopCart> getCached(@NotNull String playerName) {
        return Optional.ofNullable(cachedShopCarts.get(playerName));
    }

    public @NotNull CompletableFuture<ShopCart> loadAndCache(@NotNull OfflinePlayer bukkitPlayer) {
        return databaseManager.getOrCreateCustomer(bukkitPlayer).thenApply(customer -> {
            ShopCart shopCart = new ShopCart(customer);
            cachedShopCarts.put(bukkitPlayer.getName(), shopCart);
            return shopCart;
        });
    }

    public boolean unloadCached(@NotNull String playerName) {
        return cachedShopCarts.remove(playerName) != null;
    }

}
