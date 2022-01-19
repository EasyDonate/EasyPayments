package ru.easydonate.easypayments.database.credentials.local;

import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import ru.easydonate.easypayments.database.DatabaseType;
import ru.easydonate.easypayments.exception.DriverNotFoundException;

public final class SQLiteDatabaseCredentials extends AbstractLocalDatabaseCredentials {

    public static final String DRIVER_CLASS = "org.sqlite.JDBC";
    public static final String URL_PATTERN = "jdbc:sqlite:%s%s";

    public SQLiteDatabaseCredentials() {
        super(DatabaseType.SQLITE);
    }

    @Override
    public @NotNull String getConnectionUrl() {
        return String.format(URL_PATTERN, filePath, formatParameters());
    }

    @Override
    public void loadDriver(@NotNull Plugin plugin) throws DriverNotFoundException {
        checkDriver(plugin, DRIVER_CLASS);
    }

}
