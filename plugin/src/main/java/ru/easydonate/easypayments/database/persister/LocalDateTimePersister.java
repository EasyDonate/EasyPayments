package ru.easydonate.easypayments.database.persister;

import com.j256.ormlite.field.FieldType;
import com.j256.ormlite.field.SqlType;
import com.j256.ormlite.field.types.StringType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class LocalDateTimePersister extends StringType {

    private static final LocalDateTimePersister SINGLETON = new LocalDateTimePersister();
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    private LocalDateTimePersister() {
        super(SqlType.STRING, new Class<?>[] { LocalDateTime.class });
    }

    public static @NotNull LocalDateTimePersister getSingleton() {
        return SINGLETON;
    }

    @Override
    public @Nullable String javaToSqlArg(@NotNull FieldType fieldType, @Nullable Object javaObject) {
        if(javaObject instanceof LocalDateTime) {
            LocalDateTime asLocalDateTime = (LocalDateTime) javaObject;
            return DATE_TIME_FORMATTER.format(asLocalDateTime);
        }

        return null;
    }

    @Override
    public @Nullable LocalDateTime sqlArgToJava(@NotNull FieldType fieldType, @Nullable Object sqlArg, int columnPos) {
        if(sqlArg instanceof String) {
            String asString = (String) sqlArg;
            return LocalDateTime.from(DATE_TIME_FORMATTER.parse(asString));
        }

        return null;
    }

}
