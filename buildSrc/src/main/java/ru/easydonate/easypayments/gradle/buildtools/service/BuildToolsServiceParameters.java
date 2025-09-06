package ru.easydonate.easypayments.gradle.buildtools.service;

import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.services.BuildServiceParameters;
import org.jetbrains.annotations.NotNull;

public interface BuildToolsServiceParameters extends BuildServiceParameters {

    @NotNull DirectoryProperty getWorkingDirectory();

}
