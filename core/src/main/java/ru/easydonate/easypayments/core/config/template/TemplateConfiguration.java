package ru.easydonate.easypayments.core.config.template;

import lombok.Getter;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

@Getter
public class TemplateConfiguration extends TemplateConfigurationBase {

    private final String fileName;

    public TemplateConfiguration(@NotNull Plugin plugin, @NotNull String fileName) {
        super(plugin);
        this.fileName = fileName;
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
