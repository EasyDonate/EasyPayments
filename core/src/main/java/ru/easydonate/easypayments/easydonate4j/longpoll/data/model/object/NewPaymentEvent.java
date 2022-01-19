package ru.easydonate.easypayments.easydonate4j.longpoll.data.model.object;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import ru.easydonate.easypayments.easydonate4j.longpoll.data.model.EventObject;

import java.util.List;
import java.util.Objects;

@Getter
@NoArgsConstructor
public final class NewPaymentEvent implements EventObject {

    @SerializedName("payment_id")
    private int paymentId;

    @SerializedName("commands")
    private List<String> commands;

    @Override
    public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;

        NewPaymentEvent that = (NewPaymentEvent) o;
        return paymentId == that.paymentId &&
                Objects.equals(commands, that.commands);
    }

    @Override
    public int hashCode() {
        return Objects.hash(paymentId, commands);
    }

    @Override
    public @NotNull String toString() {
        return "NewPaymentEvent{" +
                "paymentId=" + paymentId +
                ", commands=" + commands +
                '}';
    }

}
