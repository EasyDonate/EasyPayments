package ru.easydonate.easypayments.database.persister;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.internal.$Gson$Types;
import com.j256.ormlite.field.FieldType;
import com.j256.ormlite.field.SqlType;
import com.j256.ormlite.field.types.LongStringType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;
import java.util.List;

public final class JsonArrayPersister extends LongStringType {

    private static final JsonArrayPersister SINGLETON = new JsonArrayPersister();

    private final Gson gson;
    private final Type genericType;

    private JsonArrayPersister() {
        super(SqlType.LONG_STRING, new Class<?>[]{ List.class });

        this.gson = new GsonBuilder()
                .disableHtmlEscaping()
                .create();

        this.genericType = $Gson$Types.newParameterizedTypeWithOwner(null, List.class, String.class);
    }

    public static @NotNull JsonArrayPersister getSingleton() {
        return SINGLETON;
    }

    @Override
    public @Nullable String javaToSqlArg(@NotNull FieldType fieldType, @Nullable Object javaObject) {
        try {
            if(javaObject instanceof List) {
                List<?> asList = (List<?>) javaObject;
                return gson.toJson(asList);
            }
        } catch (JsonSyntaxException ignored) {
        }

        return null;
    }

    @Override
    public @Nullable List<String> sqlArgToJava(@NotNull FieldType fieldType, @Nullable Object sqlArg, int columnPos) {
        try {
            if(sqlArg instanceof String) {
                String asString = (String) sqlArg;
                return gson.fromJson(asString, genericType);
            }
        } catch (JsonParseException ignored) {
        }

        return null;
    }

}
