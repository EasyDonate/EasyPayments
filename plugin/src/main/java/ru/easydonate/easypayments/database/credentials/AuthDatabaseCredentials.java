package ru.easydonate.easypayments.database.credentials;

import org.jetbrains.annotations.NotNull;

public interface AuthDatabaseCredentials extends DatabaseCredentials {

    @NotNull String getUsername();

    @NotNull String getPassword();

}
