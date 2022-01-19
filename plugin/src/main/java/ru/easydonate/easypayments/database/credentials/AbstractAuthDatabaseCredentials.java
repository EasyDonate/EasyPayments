package ru.easydonate.easypayments.database.credentials;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import ru.easydonate.easypayments.database.DatabaseType;

@Getter
public abstract class AbstractAuthDatabaseCredentials extends AbstractDatabaseCredentials implements AuthDatabaseCredentials {

    @CredentialField("username")
    protected String username;

    @CredentialField("password")
    protected String password;

    protected AbstractAuthDatabaseCredentials(@NotNull DatabaseType databaseType) {
        super(databaseType);
    }

}
