package ru.easydonate.easypayments.easydonate4j.json.serialization;

import com.google.gson.*;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.easydonate.easypayments.easydonate4j.PluginEventType;

import java.lang.reflect.Type;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PluginEventTypeAdapter implements JsonSerializer<PluginEventType>, JsonDeserializer<PluginEventType> {

    private static final PluginEventTypeAdapter SINGLETON = new PluginEventTypeAdapter();

    public static @NotNull PluginEventTypeAdapter getSingleton() {
        return SINGLETON;
    }

    @Override
    public @NotNull PluginEventType deserialize(
            @NotNull JsonElement json,
            @NotNull Type type,
            @NotNull JsonDeserializationContext context
    ) throws JsonParseException {
        String asString = json.getAsString();
        return PluginEventType.getByKey(asString);
    }

    @Override
    public @NotNull JsonElement serialize(
            @Nullable PluginEventType eventType,
            @NotNull Type type,
            @NotNull JsonSerializationContext context
    ) {
        return context.serialize(eventType != null ? eventType.getKey() : null);
    }

}
