package ru.easydonate.easypayments.easydonate4j.extension.data.model.object;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import ru.easydonate.easypayments.easydonate4j.extension.data.model.EventReportObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Getter
public final class NewWithdrawReport extends EventReportObject implements CommandReporting {

    @SerializedName("withdraw_id")
    private final int withdrawId;

    @SerializedName("commands")
    private List<CommandReport> commandReports;

    public NewWithdrawReport(int withdrawId) {
        this.withdrawId = withdrawId;
    }

    @Override
    public synchronized void addCommandReport(@NotNull CommandReport commandReport) {
        if(commandReports == null)
            commandReports = new ArrayList<>();

        commandReports.add(commandReport);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        NewWithdrawReport that = (NewWithdrawReport) o;
        return withdrawId == that.withdrawId &&
                Objects.equals(commandReports, that.commandReports);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), withdrawId, commandReports);
    }

    @Override
    public @NotNull String toString() {
        return "NewWithdrawReport{" +
                "pluginEventReports=" + pluginEventReports +
                ", withdrawId=" + withdrawId +
                ", commandReports=" + commandReports +
                '}';
    }

}
