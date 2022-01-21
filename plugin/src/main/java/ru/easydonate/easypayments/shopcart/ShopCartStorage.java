package ru.easydonate.easypayments.shopcart;

import lombok.Getter;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;
import ru.easydonate.easypayments.database.DatabaseManager;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Getter
public final class ShopCartStorage {

    private final DatabaseManager databaseManager;
    private final Map<UUID, ShopCart> cachedShopCarts;

    public ShopCartStorage(@NotNull DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
        this.cachedShopCarts = new ConcurrentHashMap<>();
    }

    public @NotNull @UnmodifiableView Map<UUID, ShopCart> getCachedShopCarts() {
        return Collections.unmodifiableMap(cachedShopCarts);
    }

    public @NotNull Optional<ShopCart> getCached(@NotNull UUID playerUUID) {
        return Optional.ofNullable(cachedShopCarts.get(playerUUID));
    }

    public @NotNull CompletableFuture<ShopCart> loadAndCache(@NotNull OfflinePlayer bukkitPlayer) {
        return databaseManager.getOrCreateCustomer(bukkitPlayer).thenApply(customer -> {
            ShopCart shopCart = new ShopCart(customer);
            cachedShopCarts.put(bukkitPlayer.getUniqueId(), shopCart);
            return shopCart;
        });
    }

    public boolean unloadCached(@NotNull UUID playerUUID) {
        return cachedShopCarts.remove(playerUUID) != null;
    }

}
