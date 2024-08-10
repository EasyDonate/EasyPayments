package ru.easydonate.easypayments.core.easydonate4j.longpoll.data.model.plugin;

import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import ru.easydonate.easydonate4j.data.model.PrettyPrintable;
import ru.easydonate.easypayments.core.easydonate4j.PluginEventType;
import ru.easydonate.easypayments.core.easydonate4j.json.serialization.PluginEventDeserializer;
import ru.easydonate.easypayments.core.easydonate4j.json.serialization.PluginEventTypeAdapter;

import java.util.Objects;

@Getter
@JsonAdapter(PluginEventDeserializer.class)
public abstract class PluginEvent implements PrettyPrintable {

    @JsonAdapter(PluginEventTypeAdapter.class)
    @SerializedName("type")
    protected PluginEventType pluginType;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PluginEvent that = (PluginEvent) o;
        return pluginType == that.pluginType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(pluginType);
    }

}
