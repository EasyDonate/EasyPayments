package ru.easydonate.easypayments.event;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.easydonate.easypayments.easydonate4j.longpoll.data.model.EventObject;
import ru.easydonate.easypayments.easydonate4j.longpoll.data.model.object.NewPaymentEvent;
import ru.easydonate.easypayments.easydonate4j.longpoll.data.model.object.NewRewardEvent;
import ru.easydonate.easypayments.easydonate4j.longpoll.data.model.object.NewWithdrawEvent;
import ru.easydonate.easypayments.easydonate4j.longpoll.data.model.object.RepeatPaymentEvent;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum EventType {

    NEW_PAYMENT("new_payment", NewPaymentEvent.class),
    REPEAT_PAYMENT("repeat_payment", RepeatPaymentEvent.class),
    NEW_WITHDRAW("new_withdraw", NewWithdrawEvent.class),
    NEW_REWARD("new_reward", NewRewardEvent.class),
    UNKNOWN("unknown", null);

    private final String key;
    private final Class<? extends EventObject> eventObjectClass;

    public static @NotNull EventType getByKey(@Nullable String key) {
        if(key == null || key.isEmpty())
            return UNKNOWN;

        for(EventType eventType : values())
            if(eventType.getKey().equalsIgnoreCase(key))
                return eventType;

        return UNKNOWN;
    }

}
