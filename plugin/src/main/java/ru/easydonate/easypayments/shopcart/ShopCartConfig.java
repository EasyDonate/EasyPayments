package ru.easydonate.easypayments.shopcart;

import org.jetbrains.annotations.NotNull;
import ru.easydonate.easypayments.core.config.Configuration;

import java.util.List;

public final class ShopCartConfig {

    private final Configuration config;

    private boolean enabled;
    private boolean autoIssueOnJoin;
    private List<Integer> blacklistedProductIds;
    private boolean blacklistReversed;

    public ShopCartConfig(@NotNull Configuration config) {
        this.config = config;
    }

    public void reload() {
        this.enabled = config.getBoolean("shop-cart.enabled");
        this.autoIssueOnJoin = config.getBoolean("shop-cart.auto-issue-on-join");
        this.blacklistedProductIds = config.getIntList("shop-cart.filtering.blacklist");
        this.blacklistReversed = config.getBoolean("shop-cart.filtering.use-as-whitelist");
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean shouldIssueOnJoin() {
        return enabled && autoIssueOnJoin;
    }

    public boolean shouldAddToCart(int productId) {
        if (!enabled)
            return false;

        if (blacklistReversed) {
            return blacklistedProductIds != null && blacklistedProductIds.contains(productId);
        } else {
            return blacklistedProductIds == null || !blacklistedProductIds.contains(productId);
        }
    }

}
