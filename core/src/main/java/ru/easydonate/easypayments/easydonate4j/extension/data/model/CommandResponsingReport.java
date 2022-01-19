package ru.easydonate.easypayments.easydonate4j.extension.data.model;

import org.jetbrains.annotations.Nullable;

public interface CommandResponsingReport {

    void addCommandResponse(@Nullable String response);

}
