package ru.easydonate.easypayments.core.easydonate4j.longpoll.data.model;

import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.easydonate.easydonate4j.data.model.PrettyPrintable;
import ru.easydonate.easypayments.core.easydonate4j.json.serialization.LocalDateTimeAdapter;
import ru.easydonate.easypayments.core.easydonate4j.longpoll.data.model.plugin.PluginEvent;
import ru.easydonate.easypayments.core.exception.StructureValidationException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Getter
public abstract class EventObject implements PrettyPrintable {

    @SerializedName("customer")
    protected String customer;

    @JsonAdapter(LocalDateTimeAdapter.class)
    @SerializedName("created_at")
    protected LocalDateTime createdAt;

    @SerializedName("plugins")
    protected List<PluginEvent> pluginEvents;

    public boolean hasPluginEvents() {
        return pluginEvents != null && !pluginEvents.isEmpty();
    }

    public void validate() throws StructureValidationException {
        if (customer == null)
            validationFail("'customer' = null");

        if (customer.isEmpty())
            validationFail("'customer' is empty");
    }

    protected void validationFail(@NotNull String message, @Nullable Object... args) throws StructureValidationException {
        throw new StructureValidationException(this, message, args);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        EventObject that = (EventObject) o;
        return Objects.equals(customer, that.customer) &&
                Objects.equals(createdAt, that.createdAt) &&
                Objects.equals(pluginEvents, that.pluginEvents);
    }

    @Override
    public int hashCode() {
        return Objects.hash(customer, createdAt, pluginEvents);
    }

    @Override
    public @NotNull String toString() {
        return "EventObject{" +
                "customer='" + customer + '\'' +
                ", createdAt=" + createdAt +
                ", pluginEvents=" + pluginEvents +
                '}';
    }

}
