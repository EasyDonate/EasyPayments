package ru.easydonate.easypayments.database.credentials.remote;

import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import ru.easydonate.easypayments.database.DatabaseType;
import ru.easydonate.easypayments.database.credentials.AbstractAuthDatabaseCredentials;
import ru.easydonate.easypayments.database.credentials.CredentialField;

import java.sql.SQLException;

@Getter
public abstract class AbstractRemoteDatabaseCredentials extends AbstractAuthDatabaseCredentials implements RemoteDatabaseCredentials {

    @CredentialField("host")
    protected String hostname;

    @CredentialField("port")
    protected int port;

    @CredentialField("name")
    protected String databaseName;

    protected AbstractRemoteDatabaseCredentials(@NotNull DatabaseType databaseType) {
        super(databaseType);
    }

    @Override
    public @NotNull ConnectionSource getConnectionSource() throws SQLException {
        return new JdbcConnectionSource(getConnectionUrl(), getUsername(), getPassword());
    }

}
