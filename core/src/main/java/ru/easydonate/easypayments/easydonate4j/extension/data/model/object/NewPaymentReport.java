package ru.easydonate.easypayments.easydonate4j.extension.data.model.object;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import ru.easydonate.easypayments.easydonate4j.extension.data.model.EventReportObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Getter
@AllArgsConstructor
public final class NewPaymentReport extends EventReportObject implements CommandReporting {

    @SerializedName("payment_id")
    private final int paymentId;

    @SerializedName("in_cart")
    private final boolean addedToCart;

    @SerializedName("commands")
    private List<CommandReport> commandReports;

    public NewPaymentReport(int paymentId, boolean addedToCart) {
        this.paymentId = paymentId;
        this.addedToCart = addedToCart;
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

        NewPaymentReport report = (NewPaymentReport) o;
        return paymentId == report.paymentId &&
                addedToCart == report.addedToCart &&
                Objects.equals(commandReports, report.commandReports);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), paymentId, addedToCart, commandReports);
    }

    @Override
    public @NotNull String toString() {
        return "NewPaymentReport{" +
                "pluginEventReports=" + pluginEventReports +
                ", paymentId=" + paymentId +
                ", addedToCart=" + addedToCart +
                ", commandReports=" + commandReports +
                '}';
    }

}
