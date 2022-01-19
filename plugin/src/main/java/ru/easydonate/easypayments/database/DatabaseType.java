package ru.easydonate.easypayments.database;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.easydonate.easypayments.database.credentials.DatabaseCredentials;
import ru.easydonate.easypayments.database.credentials.local.H2DatabaseCredentials;
import ru.easydonate.easypayments.database.credentials.local.SQLiteDatabaseCredentials;
import ru.easydonate.easypayments.database.credentials.remote.MySQLDatabaseCredentials;
import ru.easydonate.easypayments.database.credentials.remote.PostgreSQLDatabaseCredentials;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum DatabaseType {

    MYSQL("mysql", "MySQL", MySQLDatabaseCredentials.class),
    SQLITE("sqlite", "SQLite", SQLiteDatabaseCredentials.class),
    H2("h2", "H2", H2DatabaseCredentials.class),
    POSTGRESQL("postgresql", "PostgreSQL", PostgreSQLDatabaseCredentials.class),
    UNKNOWN("unknown", "Unknown", null);

    private final String key;
    private final String name;
    private final Class<? extends DatabaseCredentials> providingClass;

    public static @NotNull DatabaseType getByKey(@Nullable String key) {
        if(key != null && !key.isEmpty())
            for(DatabaseType databaseType : values())
                if(databaseType.getKey().equalsIgnoreCase(key))
                    return databaseType;

        return UNKNOWN;
    }

    public boolean isUnknown() {
        return this == UNKNOWN;
    }

}
