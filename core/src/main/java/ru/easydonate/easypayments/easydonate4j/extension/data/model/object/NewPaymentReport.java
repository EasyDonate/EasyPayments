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

    @SerializedName("customer")
    private final String customer;

    @SerializedName("commands")
    private List<CommandReport> commandReports;

    public NewPaymentReport(int paymentId, boolean addedToCart, String customer) {
        this.paymentId = paymentId;
        this.addedToCart = addedToCart;
        this.customer = customer;
    }

    public static NewPaymentReport createCartClearReport(int paymentId, String customer) {
        NewPaymentReport report = new NewPaymentReport(paymentId, false, customer);
        report.addCommandReport(
                "[EasyPayments] This payment was removed from the cart!",
                "[EasyPayments] Reason: an operator used '/cart clear'"
        );
        return report;
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
                Objects.equals(customer, report.customer) &&
                Objects.equals(commandReports, report.commandReports);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), paymentId, addedToCart, customer, commandReports);
    }

    @Override
    public @NotNull String toString() {
        return "NewPaymentReport{" +
                "pluginEventReports=" + pluginEventReports +
                ", paymentId=" + paymentId +
                ", addedToCart=" + addedToCart +
                ", customer=" + customer +
                ", commandReports=" + commandReports +
                '}';
    }

}
