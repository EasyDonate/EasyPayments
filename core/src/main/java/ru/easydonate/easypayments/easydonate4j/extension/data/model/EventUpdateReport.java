package ru.easydonate.easypayments.easydonate4j.extension.data.model;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import ru.easydonate.easypayments.event.EventType;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Getter
@AllArgsConstructor
public final class EventUpdateReport<R extends EventReportObject> implements Serializable {

    @SerializedName("type")
    private String rawType;

    @SerializedName("objects")
    private List<R> reportObjects;

    public EventUpdateReport(@NotNull EventType eventType) {
        this(eventType.getKey(), new ArrayList<>());
    }

    public EventUpdateReport(@NotNull EventType eventType, @NotNull List<R> reportObjects) {
        this(eventType.getKey(), reportObjects);
    }

    public void addReportObject(@NotNull R reportObject) {
        reportObjects.add(reportObject);
    }

    public @NotNull EventType getType() {
        return EventType.getByKey(rawType);
    }

}
