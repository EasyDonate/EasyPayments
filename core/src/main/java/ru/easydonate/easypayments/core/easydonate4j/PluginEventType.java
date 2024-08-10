package ru.easydonate.easypayments.core.easydonate4j;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.easydonate.easypayments.core.easydonate4j.longpoll.data.model.plugin.PluginEvent;
import ru.easydonate.easypayments.core.easydonate4j.longpoll.data.model.plugin.PurchaseNotificationsPluginEvent;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum PluginEventType {

    PURCHASE_NOTIFICATIONS("purchase_notifications", PurchaseNotificationsPluginEvent.class),
    UNKNOWN("unknown", null);

    private final String key;
    private final Class<? extends PluginEvent> pluginEventClass;

    public boolean isUnknown() {
        return this == UNKNOWN;
    }

    public static @NotNull PluginEventType getByKey(@Nullable String key) {
        if (key != null && !key.isEmpty())
            for (PluginEventType eventType : values())
                if (eventType.getKey().equalsIgnoreCase(key))
                    return eventType;

        return UNKNOWN;
    }

    @Override
    public @NotNull String toString() {
        return key;
    }

}