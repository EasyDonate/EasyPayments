package ru.easydonate.easypayments.core.easydonate4j.extension.data.model.plugin;

import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import ru.easydonate.easydonate4j.data.model.PrettyPrintable;
import ru.easydonate.easypayments.core.easydonate4j.PluginEventType;
import ru.easydonate.easypayments.core.easydonate4j.json.serialization.PluginEventTypeAdapter;

import java.util.Objects;

@Getter
public abstract class PluginEventReport implements PrettyPrintable {

    @JsonAdapter(PluginEventTypeAdapter.class)
    @SerializedName("type")
    protected final PluginEventType pluginType;

    protected PluginEventReport(@NotNull PluginEventType pluginType) {
        this.pluginType = pluginType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PluginEventReport that = (PluginEventReport) o;
        return pluginType == that.pluginType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(pluginType);
    }

}
