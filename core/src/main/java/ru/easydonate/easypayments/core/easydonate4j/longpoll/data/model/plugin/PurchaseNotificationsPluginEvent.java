package ru.easydonate.easypayments.core.easydonate4j.longpoll.data.model.plugin;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

@Getter
public final class PurchaseNotificationsPluginEvent extends PluginEvent {

    @SerializedName("commands")
    private List<String> commands;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        PurchaseNotificationsPluginEvent that = (PurchaseNotificationsPluginEvent) o;
        return Objects.equals(commands, that.commands);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), commands);
    }

    @Override
    public @NotNull String toString() {
        return "PurchaseNotificationsPluginEvent{" +
                "pluginType=" + pluginType +
                ", commands=" + commands +
                '}';
    }

}
