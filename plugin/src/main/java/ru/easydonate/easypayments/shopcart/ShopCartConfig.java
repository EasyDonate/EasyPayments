package ru.easydonate.easypayments.shopcart;

import org.jetbrains.annotations.NotNull;
import ru.easydonate.easypayments.core.Constants;
import ru.easydonate.easypayments.core.config.Configuration;
import ru.easydonate.easypayments.core.logging.DebugLogger;

import java.util.List;

public final class ShopCartConfig {

    private final Configuration config;
    private final DebugLogger debugLogger;

    private boolean enabled;
    private boolean autoIssueEnabled;
    private List<Integer> blacklistedProductIds;
    private boolean blacklistReversed;

    public ShopCartConfig(@NotNull Configuration config, @NotNull DebugLogger debugLogger) {
        this.config = config;
        this.debugLogger = debugLogger;
    }

    public void reload() {
        this.enabled = config.getBoolean("shop-cart.enabled", Constants.DEFAULT_SHOP_CART_STATUS);
        this.autoIssueEnabled = config.getBoolean("shop-cart.auto-issue-when-online", Constants.DEFAULT_AUTO_ISSUE_STATUS);
        this.blacklistedProductIds = config.getIntList("shop-cart.filtering.blacklist");
        this.blacklistReversed = config.getBoolean("shop-cart.filtering.use-as-whitelist", false);

        // show shop cart status in logs
        debugLogger.debug("[ShopCart] Enabled: {0}", (enabled ? "YES" : "NO"));
        debugLogger.debug("[ShopCart] Auto-issue: {0}", (autoIssueEnabled ? "YES" : "NO"));

        if (blacklistReversed) {
            debugLogger.debug("[ShopCart] Whitelisted products: {0}", blacklistedProductIds);
        } else {
            debugLogger.debug("[ShopCart] Blacklisted products: {0}", blacklistedProductIds);
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean shouldIssueWhenOnline() {
        return !enabled || autoIssueEnabled;
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
