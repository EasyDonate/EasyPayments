package ru.easydonate.easypayments.easydonate4j.longpoll.data.model;

import org.jetbrains.annotations.NotNull;
import ru.easydonate.easydonate4j.data.model.PrettyPrintable;
import ru.easydonate.easypayments.easydonate4j.extension.data.model.EventUpdateReports;

import java.util.ArrayList;

public final class EventUpdates extends ArrayList<EventUpdate<?>> implements PrettyPrintable {

    public static final EventUpdates EMPTY = new EventUpdates();

    public @NotNull EventUpdateReports createReports() {
        return new EventUpdateReports();
    }

}
