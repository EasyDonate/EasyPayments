package ru.easydonate.easypayments.core.easydonate4j.extension.data.model.object;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface CommandReporting {

    @NotNull List<CommandReport> getCommandReports();

    void addCommandReport(@NotNull CommandReport commandReport);

    default void addCommandReport(@NotNull String command, @NotNull String response) {
        addCommandReport(CommandReport.create(command, response));
    }

    default void addCommandReport(@NotNull String command, @Nullable List<String> feedbackMessages) {
        addCommandReport(CommandReport.create(command, feedbackMessages));
    }

}
