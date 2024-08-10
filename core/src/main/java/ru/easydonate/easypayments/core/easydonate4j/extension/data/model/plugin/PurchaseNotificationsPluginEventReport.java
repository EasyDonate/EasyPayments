package ru.easydonate.easypayments.core.easydonate4j.extension.data.model.plugin;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import ru.easydonate.easypayments.core.easydonate4j.PluginEventType;
import ru.easydonate.easypayments.core.easydonate4j.extension.data.model.object.CommandReport;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Getter
public final class PurchaseNotificationsPluginEventReport extends PluginEventReport {

    @SerializedName("commands")
    private final List<CommandReport> commandReports;

    public PurchaseNotificationsPluginEventReport() {
        this(new ArrayList<>());
    }

    public PurchaseNotificationsPluginEventReport(@NotNull List<CommandReport> commandReports) {
        super(PluginEventType.PURCHASE_NOTIFICATIONS);
        this.commandReports = commandReports;
    }

    public void addCommandReport(@NotNull CommandReport commandReport) {
        commandReports.add(commandReport);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        PurchaseNotificationsPluginEventReport that = (PurchaseNotificationsPluginEventReport) o;
        return Objects.equals(commandReports, that.commandReports);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), commandReports);
    }

    @Override
    public @NotNull String toString() {
        return "PurchaseNotificationsPluginEventReport{" +
                "pluginType=" + pluginType +
                ", commandReports=" + commandReports +
                '}';
    }

}
