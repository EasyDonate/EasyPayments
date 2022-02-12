package ru.easydonate.easypayments.api.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public abstract class AsyncBukkitEvent extends Event {

    private static final HandlerList HANDLER_LIST = new HandlerList();

    protected AsyncBukkitEvent() {
        super(true);
    }

    public static @NotNull HandlerList getHandlerList() {
        return HANDLER_LIST;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLER_LIST;
    }

}
