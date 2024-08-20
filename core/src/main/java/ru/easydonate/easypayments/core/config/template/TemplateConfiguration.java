package ru.easydonate.easypayments.core.config.template;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import ru.easydonate.easypayments.core.EasyPayments;

import java.nio.file.Path;

@Getter
public class TemplateConfiguration extends TemplateConfigurationBase {

    private final String fileName;

    public TemplateConfiguration(@NotNull EasyPayments plugin, @NotNull String fileName) {
        super(plugin);
        this.fileName = fileName;
    }

    @Override
    public String getName() {
        return fileName;
    }

    @Override
    protected @NotNull String getResourcePath() {
        return fileName;
    }

    @Override
    protected @NotNull Path getOutputFile() {
        return plugin.getDataFolder().toPath().resolve(fileName);
    }

}
