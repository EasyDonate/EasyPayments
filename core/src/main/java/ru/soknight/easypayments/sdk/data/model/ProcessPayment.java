package ru.soknight.easypayments.sdk.data.model;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;

import java.util.List;
import java.util.Objects;

@Getter
public class ProcessPayment {

    @SerializedName("id")
    private int paymentId;
    @SerializedName("payload")
    private String payload;
    @SerializedName("commands")
    private List<String> commands;

    @Override
    public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;

        ProcessPayment that = (ProcessPayment) o;
        return paymentId == that.paymentId &&
                Objects.equals(payload, that.payload) &&
                Objects.equals(commands, that.commands);
    }

    @Override
    public int hashCode() {
        return Objects.hash(paymentId, payload, commands);
    }

}
