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
    private final List<CommandReport> commandReports;

    public NewPaymentReport(int paymentId, boolean addedToCart) {
        this.paymentId = paymentId;
        this.addedToCart = addedToCart;
        this.commandReports = addedToCart ? null : new ArrayList<>();
    }

    @Override
    public void addCommandReport(@NotNull CommandReport commandReport) {
        commandReports.add(commandReport);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        NewPaymentReport that = (NewPaymentReport) o;
        return paymentId == that.paymentId &&
                addedToCart == that.addedToCart &&
                Objects.equals(commandReports, that.commandReports);
    }

    @Override
    public int hashCode() {
        return Objects.hash(paymentId, addedToCart, commandReports);
    }

    @Override
    public @NotNull String toString() {
        return "NewPaymentReport{" +
                "paymentId=" + paymentId +
                ", addedToCart=" + addedToCart +
                ", commandReports=" + commandReports +
                '}';
    }

}
