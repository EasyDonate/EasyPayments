package ru.easydonate.easypayments.core.easydonate4j.extension.data.model;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import ru.easydonate.easydonate4j.data.model.PrettyPrintable;
import ru.easydonate.easypayments.core.easydonate4j.extension.data.model.plugin.PluginEventReport;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Getter
public abstract class EventReportObject implements PrettyPrintable {

    @SerializedName("plugins")
    protected List<PluginEventReport> pluginEventReports;

    public synchronized void addPluginEventReport(@NotNull PluginEventReport pluginEventReport) {
        if (pluginEventReports == null)
            pluginEventReports = new ArrayList<>();

        pluginEventReports.add(pluginEventReport);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        EventReportObject that = (EventReportObject) o;
        return Objects.equals(pluginEventReports, that.pluginEventReports);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pluginEventReports);
    }

    @Override
    public @NotNull String toString() {
        return "EventReportObject{" +
                "pluginEventReports=" + pluginEventReports +
                '}';
    }

}
