package ru.easydonate.easypayments.easydonate4j.extension.data.model.object;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.easydonate.easypayments.easydonate4j.extension.data.model.CommandResponsingReport;
import ru.easydonate.easypayments.easydonate4j.extension.data.model.EventReportObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Getter
@NoArgsConstructor
public final class RepeatPaymentEventReport implements EventReportObject, CommandResponsingReport {

    @SerializedName("payment_id")
    private int paymentId;

    @SerializedName("responses")
    private List<String> responses;

    public RepeatPaymentEventReport(int paymentId) {
        this.paymentId = paymentId;
        this.responses = new ArrayList<>();
    }

    @Override
    public void addCommandResponse(@Nullable String response) {
        responses.add(response);
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;

        RepeatPaymentEventReport that = (RepeatPaymentEventReport) o;
        return paymentId == that.paymentId &&
                Objects.equals(responses, that.responses);
    }

    @Override
    public int hashCode() {
        return Objects.hash(paymentId, responses);
    }

    @Override
    public @NotNull String toString() {
        return "RepeatPaymentEventReport{" +
                "paymentId=" + paymentId +
                ", responses=" + responses +
                '}';
    }

}
