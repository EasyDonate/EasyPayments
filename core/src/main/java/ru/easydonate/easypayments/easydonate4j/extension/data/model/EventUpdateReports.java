package ru.easydonate.easypayments.easydonate4j.extension.data.model;

import org.jetbrains.annotations.NotNull;
import ru.easydonate.easydonate4j.data.model.PrettyPrintable;

import java.util.ArrayList;
import java.util.Collection;

public final class EventUpdateReports extends ArrayList<EventUpdateReport<?>> implements PrettyPrintable {

    public static final EventUpdateReports EMPTY = new EventUpdateReports();

    public EventUpdateReports() {
    }

    public EventUpdateReports(@NotNull EventUpdateReport<?> content) {
        add(content);
    }

    public EventUpdateReports(@NotNull Collection<? extends EventUpdateReport<?>> content) {
        super(content);
    }

}
