package ru.easydonate.easypayments.database.credentials.local;

import org.jetbrains.annotations.NotNull;
import ru.easydonate.easypayments.database.credentials.DatabaseCredentials;

public interface LocalDatabaseCredentials extends DatabaseCredentials {

    @NotNull String getFilePath();

}
