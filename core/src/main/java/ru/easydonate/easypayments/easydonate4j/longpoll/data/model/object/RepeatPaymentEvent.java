package ru.easydonate.easypayments.easydonate4j.longpoll.data.model.object;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import ru.easydonate.easypayments.easydonate4j.longpoll.data.model.EventObject;

import java.util.List;
import java.util.Objects;

@Getter
public final class RepeatPaymentEvent extends EventObject {

    @SerializedName("payment_id")
    private int paymentId;

    @SerializedName("products")
    private List<PurchasedProduct> products;

    @Override
    public void validate() {
        super.validate();

        if(paymentId <= 0)
            validationFail("'paymentId' must be >0, but it's %d", paymentId);

        if(products == null || products.isEmpty())
            validationFail("no products list");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        RepeatPaymentEvent that = (RepeatPaymentEvent) o;
        return paymentId == that.paymentId &&
                Objects.equals(products, that.products);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), paymentId, products);
    }

    @Override
    public @NotNull String toString() {
        return "RepeatPaymentEvent{" +
                "customer='" + customer + '\'' +
                ", createdAt=" + createdAt +
                ", paymentId=" + paymentId +
                ", products=" + products +
                '}';
    }

}
