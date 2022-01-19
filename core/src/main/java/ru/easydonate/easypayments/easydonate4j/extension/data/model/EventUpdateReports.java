package ru.easydonate.easypayments.easydonate4j.extension.data.model;

import ru.easydonate.easydonate4j.data.model.PrettyPrintable;

import java.util.ArrayList;

public final class EventUpdateReports extends ArrayList<EventUpdateReport<?>> implements PrettyPrintable {

    public static final EventUpdateReports EMPTY = new EventUpdateReports();

}
