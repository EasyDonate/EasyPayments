package ru.easydonate.easypayments.database.credentials.remote;

import org.jetbrains.annotations.NotNull;
import ru.easydonate.easypayments.database.credentials.DatabaseCredentials;

public interface RemoteDatabaseCredentials extends DatabaseCredentials {

    @NotNull String getHostname();

    int getPort();

}
