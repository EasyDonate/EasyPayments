package ru.easydonate.easypayments.easydonate4j.extension.data.model.object;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import ru.easydonate.easypayments.easydonate4j.extension.data.model.EventReportObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Getter
public final class RepeatPaymentReport extends EventReportObject implements CommandReporting {

    @SerializedName("payment_id")
    private final int paymentId;

    @SerializedName("commands")
    private final List<CommandReport> commandReports;

    public RepeatPaymentReport(int paymentId) {
        this.paymentId = paymentId;
        this.commandReports = new ArrayList<>();
    }

    @Override
    public void addCommandReport(@NotNull CommandReport commandReport) {
        commandReports.add(commandReport);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RepeatPaymentReport that = (RepeatPaymentReport) o;
        return paymentId == that.paymentId &&
                Objects.equals(commandReports, that.commandReports);
    }

    @Override
    public int hashCode() {
        return Objects.hash(paymentId, commandReports);
    }

    @Override
    public @NotNull String toString() {
        return "RepeatPaymentReport{" +
                "paymentId=" + paymentId +
                ", commandReports=" + commandReports +
                '}';
    }

}
