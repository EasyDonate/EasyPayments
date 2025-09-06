package ru.easydonate.easypayments.gradle.spigotmapper;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.jetbrains.annotations.NotNull;
import ru.easydonate.easypayments.gradle.spigotmapper.extension.SpigotMapperExtension;

public abstract class SpigotMapperPlugin implements Plugin<Project> {

    @Override
    public void apply(@NotNull Project project) {
        project.getExtensions().create("spigotMapper", SpigotMapperExtension.class);
    }

}
