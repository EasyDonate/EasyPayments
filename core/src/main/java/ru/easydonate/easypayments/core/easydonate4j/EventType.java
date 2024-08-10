package ru.easydonate.easypayments.core.easydonate4j;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.easydonate.easypayments.core.easydonate4j.longpoll.data.model.EventObject;
import ru.easydonate.easypayments.core.easydonate4j.longpoll.data.model.object.NewPaymentEvent;
import ru.easydonate.easypayments.core.easydonate4j.longpoll.data.model.object.NewRewardEvent;
import ru.easydonate.easypayments.core.easydonate4j.longpoll.data.model.object.NewWithdrawEvent;
import ru.easydonate.easypayments.core.easydonate4j.longpoll.data.model.object.RepeatPaymentEvent;

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

    public boolean isUnknown() {
        return this == UNKNOWN;
    }

    public static @NotNull EventType getByKey(@Nullable String key) {
        if(key != null && !key.isEmpty())
            for(EventType eventType : values())
                if(eventType.getKey().equalsIgnoreCase(key))
                    return eventType;

        return UNKNOWN;
    }

    @Override
    public @NotNull String toString() {
        return key;
    }

}

