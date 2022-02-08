package ru.easydonate.easypayments.database.credentials.local;

import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import lombok.Getter;
import org.apache.commons.lang.Validate;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import ru.easydonate.easypayments.database.DatabaseType;
import ru.easydonate.easypayments.database.credentials.AbstractDatabaseCredentials;
import ru.easydonate.easypayments.database.credentials.CredentialField;

import java.sql.SQLException;

@Getter
public abstract class AbstractLocalDatabaseCredentials extends AbstractDatabaseCredentials implements LocalDatabaseCredentials {

    protected final Plugin plugin;

    @CredentialField("file")
    protected String filePath;

    protected AbstractLocalDatabaseCredentials(@NotNull Plugin plugin, @NotNull DatabaseType databaseType) {
        super(databaseType);
        this.plugin = plugin;
    }

    public @NotNull String getFilePath() {
        Validate.notEmpty(filePath, "filePath");
        return plugin.getDataFolder().toPath().resolve(filePath).toAbsolutePath().toString();
    }

    @Override
    public @NotNull ConnectionSource getConnectionSource() throws SQLException {
        return new JdbcConnectionSource(getConnectionUrl());
    }

}
