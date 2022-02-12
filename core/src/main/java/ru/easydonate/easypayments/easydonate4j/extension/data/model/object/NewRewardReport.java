package ru.easydonate.easypayments.easydonate4j.extension.data.model.object;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import ru.easydonate.easypayments.easydonate4j.extension.data.model.EventReportObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Getter
public final class NewRewardReport extends EventReportObject implements CommandReporting {

    @SerializedName("reward_id")
    private final int rewardId;

    @SerializedName("commands")
    private List<CommandReport> commandReports;

    public NewRewardReport(int rewardId) {
        this.rewardId = rewardId;
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

        NewRewardReport that = (NewRewardReport) o;
        return rewardId == that.rewardId &&
                Objects.equals(commandReports, that.commandReports);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), rewardId, commandReports);
    }

    @Override
    public @NotNull String toString() {
        return "NewRewardReport{" +
                "pluginEventReports=" + pluginEventReports +
                ", rewardId=" + rewardId +
                ", commandReports=" + commandReports +
                '}';
    }

}
