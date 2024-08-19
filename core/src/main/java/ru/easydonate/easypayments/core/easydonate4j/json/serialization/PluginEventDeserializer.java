package ru.easydonate.easypayments.core.easydonate4j.json.serialization;

import com.google.gson.*;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.easydonate.easypayments.core.easydonate4j.PluginEventType;
import ru.easydonate.easypayments.core.easydonate4j.longpoll.data.model.plugin.PluginEvent;

import java.lang.reflect.Type;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PluginEventDeserializer implements JsonDeserializer<PluginEvent> {

    private static final PluginEventDeserializer SINGLETON = new PluginEventDeserializer();

    public static @NotNull PluginEventDeserializer getSingleton() {
        return SINGLETON;
    }

    @Override
    public @Nullable PluginEvent deserialize(
            @NotNull JsonElement json,
            @NotNull Type objectType,
            @NotNull JsonDeserializationContext context
    ) throws JsonParseException {
        return deserialize0(json, objectType, context);
    }

    @SneakyThrows
    private <E extends PluginEvent> @Nullable E deserialize0(
            @NotNull JsonElement json,
            @NotNull Type objectType,
            @NotNull JsonDeserializationContext context
    ) throws JsonParseException {
        JsonObject rootObject = json.getAsJsonObject();

        String rawType = rootObject.get("type").getAsString();
        PluginEventType type = PluginEventType.getByKey(rawType);
        if (type == PluginEventType.UNKNOWN)
            return null;

        Class<?> pluginEventClass = type.getPluginEventClass();
        return context.deserialize(rootObject, pluginEventClass);
    }

}
