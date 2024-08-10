package ru.easydonate.easypayments.core.easydonate4j.json.serialization;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import ru.easydonate.easypayments.core.easydonate4j.longpoll.data.model.EventObject;
import ru.easydonate.easypayments.core.easydonate4j.EventType;
import ru.easydonate.easypayments.core.easydonate4j.longpoll.data.model.EventUpdate;

import java.lang.reflect.Type;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class EventUpdateDeserializer implements JsonDeserializer {

    private static final EventUpdateDeserializer SINGLETON = new EventUpdateDeserializer();

    public static @NotNull EventUpdateDeserializer getSingleton() {
        return SINGLETON;
    }

    @Override
    public EventUpdate<?> deserialize(JsonElement json, Type objectType, JsonDeserializationContext context) throws JsonParseException {
        return deserialize0(json, objectType, context);
    }

    @SuppressWarnings("unchecked")
    private <E extends EventObject> EventUpdate<E> deserialize0(JsonElement json, Type objectType, JsonDeserializationContext context) throws JsonParseException {
        JsonObject rootObject = json.getAsJsonObject();

        String rawType = rootObject.get("type").getAsString();
        EventType type = EventType.getByKey(rawType);
        if (type == EventType.UNKNOWN)
            return null;

        List<E> eventObjects = null;
        if (rootObject.has("objects")) {
            JsonArray rawEventObjects = rootObject.getAsJsonArray("objects");
            Class<E> eventObjectClass = (Class<E>) type.getEventObjectClass();
            eventObjects = context.deserialize(rawEventObjects, TypeToken.getParameterized(List.class, eventObjectClass).getType());
        }

        return new EventUpdate<>(type, eventObjects);
    }

}
