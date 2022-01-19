package ru.easydonate.easypayments.easydonate4j.longpoll.data.model;

import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import ru.easydonate.easydonate4j.data.model.PrettyPrintable;
import ru.easydonate.easypayments.easydonate4j.longpoll.json.deserializer.EventUpdateDeserializer;
import ru.easydonate.easypayments.event.EventType;

import java.util.List;
import java.util.Objects;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@JsonAdapter(EventUpdateDeserializer.class)
public final class EventUpdate<E extends EventObject> implements PrettyPrintable {

    @SerializedName("type")
    private String rawType;

    @SerializedName("objects")
    private List<E> eventObjects;

    public @NotNull EventType getType() {
        return EventType.getByKey(rawType);
    }

    public boolean hasUnknownType() {
        return getType() == EventType.UNKNOWN;
    }

    public boolean hasEventObjects() {
        return eventObjects != null && !eventObjects.isEmpty();
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;

        EventUpdate<?> that = (EventUpdate<?>) o;
        return Objects.equals(rawType, that.rawType) &&
                Objects.equals(eventObjects, that.eventObjects);
    }

    @Override
    public int hashCode() {
        return Objects.hash(rawType, eventObjects);
    }

    @Override
    public @NotNull String toString() {
        return "EventUpdate{" +
                "type=" + getType() +
                ", eventObjects=" + eventObjects +
                '}';
    }

}
