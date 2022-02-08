package ru.easydonate.easypayments.easydonate4j.longpoll.data.model.object;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import ru.easydonate.easypayments.easydonate4j.longpoll.data.model.EventObject;
import ru.easydonate.easypayments.exception.StructureValidationException;

import java.util.List;
import java.util.Objects;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public final class NewPaymentEvent extends EventObject {

    @SerializedName("payment_id")
    private int paymentId;

    @SerializedName("products")
    private List<PurchasedProduct> products;

    @Override
    public void validate() throws StructureValidationException {
        super.validate();

        if(paymentId <= 0)
            validationFail("'paymentId' must be > 0, but it's %d", paymentId);

        if(products == null || products.isEmpty())
            validationFail("no products present");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        NewPaymentEvent that = (NewPaymentEvent) o;
        return paymentId == that.paymentId &&
                Objects.equals(products, that.products);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), paymentId, products);
    }

    @Override
    public @NotNull String toString() {
        return "NewPaymentEvent{" +
                "customer='" + customer + '\'' +
                ", createdAt=" + createdAt +
                ", paymentId=" + paymentId +
                ", products=" + products +
                '}';
    }

}
