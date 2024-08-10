package ru.easydonate.easypayments.core.easydonate4j.json.serialization;

import com.google.gson.*;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.easydonate.easypayments.core.easydonate4j.EventType;

import java.lang.reflect.Type;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class EventTypeAdapter implements JsonSerializer<EventType>, JsonDeserializer<EventType> {

    private static final EventTypeAdapter SINGLETON = new EventTypeAdapter();

    public static @NotNull EventTypeAdapter getSingleton() {
        return SINGLETON;
    }

    @Override
    public @NotNull EventType deserialize(
            @NotNull JsonElement json,
            @NotNull Type type,
            @NotNull JsonDeserializationContext context
    ) throws JsonParseException {
        String asString = json.getAsString();
        return EventType.getByKey(asString);
    }

    @Override
    public @NotNull JsonElement serialize(
            @Nullable EventType eventType,
            @NotNull Type type,
            @NotNull JsonSerializationContext context
    ) {
        return context.serialize(eventType != null ? eventType.getKey() : null);
    }

}
