package ru.easydonate.easypayments.easydonate4j.extension.data.model.object;

import com.google.gson.annotations.SerializedName;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.easydonate.easydonate4j.data.model.PrettyPrintable;

import java.util.List;
import java.util.Objects;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class CommandReport implements PrettyPrintable {

    @SerializedName("command")
    private final String command;

    @SerializedName("response")
    private final String response;

    public static @NotNull CommandReport create(@NotNull String command, @NotNull String response) {
        return new CommandReport(command, response);
    }

    public static @NotNull CommandReport create(@NotNull String command, @Nullable List<String> feedbackMessages) {
        return new CommandReport(command, feedbackMessages != null ? String.join("\n", feedbackMessages) : "");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CommandReport that = (CommandReport) o;
        return Objects.equals(command, that.command) &&
                Objects.equals(response, that.response);
    }

    @Override
    public int hashCode() {
        return Objects.hash(command, response);
    }

    @Override
    public @NotNull String toString() {
        return "CommandReport{" +
                "command='" + command + '\'' +
                ", response='" + response + '\'' +
                '}';
    }

}
