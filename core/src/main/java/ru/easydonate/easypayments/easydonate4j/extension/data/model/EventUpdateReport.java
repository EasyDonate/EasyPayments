package ru.easydonate.easypayments.easydonate4j.extension.data.model;

import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import ru.easydonate.easydonate4j.data.model.PrettyPrintable;
import ru.easydonate.easypayments.easydonate4j.EventType;
import ru.easydonate.easypayments.easydonate4j.json.serialization.EventTypeAdapter;

import java.util.ArrayList;
import java.util.List;

@Getter
@AllArgsConstructor
public final class EventUpdateReport<R extends EventReportObject> implements PrettyPrintable {

    @JsonAdapter(EventTypeAdapter.class)
    @SerializedName("type")
    private final EventType eventType;

    @SerializedName("objects")
    private final List<R> reportObjects;

    public EventUpdateReport(@NotNull EventType eventType) {
        this(eventType, new ArrayList<>());
    }

    public void addObject(@NotNull R reportObject) {
        reportObjects.add(reportObject);
    }

}
