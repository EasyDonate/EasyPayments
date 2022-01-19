package ru.easydonate.easypayments.database.credentials;

import com.j256.ormlite.support.ConnectionSource;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import ru.easydonate.easypayments.database.DatabaseType;
import ru.easydonate.easypayments.database.credentials.local.LocalDatabaseCredentials;
import ru.easydonate.easypayments.database.credentials.remote.RemoteDatabaseCredentials;
import ru.easydonate.easypayments.exception.CredentialsParseException;
import ru.easydonate.easypayments.exception.DriverLoadException;
import ru.easydonate.easypayments.exception.DriverNotFoundException;

import java.sql.SQLException;

public interface DatabaseCredentials {

    static @NotNull DatabaseCredentials parse(
            @NotNull ConfigurationSection config,
            @NotNull DatabaseType databaseType
    ) throws CredentialsParseException {
        return DatabaseCredentialsParser.parse(config, databaseType);
    }

    @NotNull DatabaseType getDatabaseType();

    @NotNull String getConnectionUrl();

    @NotNull ConnectionSource getConnectionSource() throws SQLException;

    void loadDriver(@NotNull Plugin plugin) throws DriverNotFoundException, DriverLoadException;

    default boolean isAuthRequired() {
        return this instanceof AuthDatabaseCredentials;
    }

    default boolean isLocalDatabase() {
        return this instanceof LocalDatabaseCredentials;
    }

    default boolean isRemoteDatabase() {
        return this instanceof RemoteDatabaseCredentials;
    }

}
