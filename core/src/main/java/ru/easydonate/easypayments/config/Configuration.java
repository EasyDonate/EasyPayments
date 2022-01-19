package ru.easydonate.easypayments.config;

import lombok.Getter;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import ru.easydonate.easypayments.exception.ConfigurationValidationException;

@Getter
public final class Configuration extends AbstractConfiguration<Configuration> {

    private final String fileName;
    private final String resourcePath;
    private Validator<Configuration> validator;

    public Configuration(@NotNull Plugin plugin, @NotNull String fileName) {
        this(plugin, fileName, String.format("/%s", fileName));
    }

    public Configuration(@NotNull Plugin plugin, @NotNull String fileName, String resourcePath) {
        super(plugin);
        this.fileName = fileName;
        this.resourcePath = resourcePath;
    }

    @Override
    protected Configuration getThis() {
        return this;
    }

    @Override
    public @NotNull Configuration reload() throws ConfigurationValidationException {
        super.reload();

        if(validator != null) {
            validator.validate(this);
        }

        return this;
    }

    public @NotNull Configuration withValidator(@NotNull Validator<Configuration> validator) {
        this.validator = validator;
        return this;
    }

}
