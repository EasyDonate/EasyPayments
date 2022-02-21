package ru.easydonate.easypayments.database.credentials.remote;

import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import ru.easydonate.easypayments.database.DatabaseType;
import ru.easydonate.easypayments.exception.DriverLoadException;
import ru.easydonate.easypayments.exception.DriverNotFoundException;

public final class PostgreSQLDatabaseCredentials extends AbstractRemoteDatabaseCredentials {

    public static final String DRIVER_DOWNLOAD_URL = "https://repo1.maven.org/maven2/org/postgresql/postgresql/42.3.3/postgresql-42.3.3.jar";
    public static final String DRIVER_FILE_CHECKSUM = "bef0b2e1c6edcd8647c24bed31e1a4ac";
    public static final String DRIVER_OUTPUT_FILE = "postgresql.jar";

    public static final String DRIVER_CLASS = "org.postgresql.Driver";
    public static final String URL_PATTERN = "jdbc:postgresql://%s:%d/%s%s";

    public PostgreSQLDatabaseCredentials() {
        super(DatabaseType.POSTGRESQL);
    }

    @Override
    public @NotNull String getConnectionUrl() {
        return String.format(URL_PATTERN, hostname, port, databaseName, formatParameters());
    }

    @Override
    public void loadDriver(@NotNull Plugin plugin) throws DriverNotFoundException, DriverLoadException {
        checkDriver(plugin, true);
    }

    @Override
    protected @NotNull String getDriverDownloadURL() {
        return DRIVER_DOWNLOAD_URL;
    }

    @Override
    protected @NotNull String getDriverFileChecksum() {
        return DRIVER_FILE_CHECKSUM;
    }

    @Override
    protected @NotNull String getDriverOutputFile() {
        return DRIVER_OUTPUT_FILE;
    }

    @Override
    protected void checkDriver(@NotNull Plugin plugin, boolean tryDownloadDriver) throws DriverNotFoundException, DriverLoadException {
        try {
            checkDriver(plugin, DRIVER_CLASS);
            if(tryDownloadDriver) {
                plugin.getLogger().info("PostgreSQL JDBC Driver is already loaded in the JVM Runtime.");
            }
        } catch (DriverNotFoundException ex) {
            if(tryDownloadDriver) {
                tryDownloadDriver(plugin);
            } else {
                throw ex;
            }
        }

    }

}
