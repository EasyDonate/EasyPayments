package ru.easydonate.easypayments.easydonate4j.longpoll.data.model;

import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import ru.easydonate.easydonate4j.data.model.PrettyPrintable;
import ru.easydonate.easypayments.easydonate4j.EventType;
import ru.easydonate.easypayments.easydonate4j.extension.data.model.EventReportObject;
import ru.easydonate.easypayments.easydonate4j.extension.data.model.EventUpdateReport;
import ru.easydonate.easypayments.easydonate4j.json.serialization.EventTypeAdapter;
import ru.easydonate.easypayments.easydonate4j.json.serialization.EventUpdateDeserializer;
import ru.easydonate.easypayments.exception.StructureValidationException;

import java.util.List;
import java.util.Objects;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@JsonAdapter(EventUpdateDeserializer.class)
public final class EventUpdate<E extends EventObject> implements PrettyPrintable {

    @JsonAdapter(EventTypeAdapter.class)
    @SerializedName("type")
    private EventType eventType;

    @SerializedName("objects")
    private List<E> eventObjects;

    public <R extends EventReportObject> @NotNull EventUpdateReport<R> createReport() {
        return new EventUpdateReport<>(eventType);
    }

    public void validate() throws StructureValidationException {
        if(hasUnknownType())
            throw new StructureValidationException(this, "no known event type present");

        if(!hasEventObjects())
            throw new StructureValidationException(this, "no event objects present");
    }

    public boolean hasUnknownType() {
        return eventType == null || eventType == EventType.UNKNOWN;
    }

    public boolean hasEventObjects() {
        return eventObjects != null && !eventObjects.isEmpty();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        EventUpdate<?> that = (EventUpdate<?>) o;
        return eventType == that.eventType &&
                Objects.equals(eventObjects, that.eventObjects);
    }

    @Override
    public int hashCode() {
        return Objects.hash(eventType, eventObjects);
    }

    @Override
    public @NotNull String toString() {
        return "EventUpdate{" +
                "eventType=" + eventType +
                ", eventObjects=" + eventObjects +
                '}';
    }

}
