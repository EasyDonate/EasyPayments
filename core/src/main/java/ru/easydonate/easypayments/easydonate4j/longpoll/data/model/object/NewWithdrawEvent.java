package ru.easydonate.easypayments.easydonate4j.longpoll.data.model.object;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import ru.easydonate.easypayments.easydonate4j.longpoll.data.model.EventObject;
import ru.easydonate.easypayments.exception.StructureValidationException;

import java.util.List;
import java.util.Objects;

@Getter
public final class NewWithdrawEvent extends EventObject {

    @SerializedName("payment_id")
    private int paymentId;

    @SerializedName("commands")
    private List<String> commands;

    @Override
    public void validate() throws StructureValidationException {
        super.validate();

        if(paymentId <= 0)
            validationFail("'paymentId' must be > 0, but it's %d", paymentId);

        if(commands == null || commands.isEmpty())
            validationFail("no commands present");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        NewWithdrawEvent that = (NewWithdrawEvent) o;
        return paymentId == that.paymentId &&
                Objects.equals(commands, that.commands);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), paymentId, commands);
    }

    @Override
    public @NotNull String toString() {
        return "NewWithdrawEvent{" +
                "customer='" + customer + '\'' +
                ", paymentId=" + paymentId +
                ", commands=" + commands +
                '}';
    }

}
