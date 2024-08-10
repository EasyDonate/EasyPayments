package ru.easydonate.easypayments.core.easydonate4j.json.serialization;

import com.google.gson.*;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class LocalDateTimeAdapter implements JsonSerializer<LocalDateTime>, JsonDeserializer<LocalDateTime> {

    private static final LocalDateTimeAdapter SINGLETON = new LocalDateTimeAdapter();
    private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static @NotNull LocalDateTimeAdapter getSingleton() {
        return SINGLETON;
    }

    @Override
    public @Nullable LocalDateTime deserialize(
            @NotNull JsonElement json,
            @NotNull Type type,
            @NotNull JsonDeserializationContext context
    ) throws JsonParseException {
        String asString = json.getAsString();
        if (asString == null || asString.isEmpty())
            return null;

        TemporalAccessor accessor = DATE_TIME_FORMAT.parse(asString);
        return LocalDateTime.from(accessor);
    }

    @Override
    public @NotNull JsonElement serialize(
            @Nullable LocalDateTime localDateTime,
            @NotNull Type type,
            @NotNull JsonSerializationContext context
    ) {
        return context.serialize(localDateTime != null ? DATE_TIME_FORMAT.format(localDateTime) : null);
    }

}
