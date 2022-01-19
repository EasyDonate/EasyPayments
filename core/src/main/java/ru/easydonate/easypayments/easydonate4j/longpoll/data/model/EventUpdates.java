package ru.easydonate.easypayments.easydonate4j.longpoll.data.model;

import ru.easydonate.easydonate4j.data.model.PrettyPrintable;

import java.util.ArrayList;

public final class EventUpdates extends ArrayList<EventUpdate<?>> implements PrettyPrintable {

    public static final EventUpdates EMPTY = new EventUpdates();

}
