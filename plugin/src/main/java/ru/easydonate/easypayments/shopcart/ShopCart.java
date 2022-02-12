package ru.easydonate.easypayments.shopcart;

import com.j256.ormlite.dao.ForeignCollection;
import lombok.Getter;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import ru.easydonate.easypayments.database.model.Customer;
import ru.easydonate.easypayments.database.model.Payment;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.stream.Collectors;

@Getter
public final class ShopCart {

    private final Customer customer;

    public ShopCart(@NotNull Customer customer) {
        this.customer = customer;
    }

    public @NotNull OfflinePlayer getCartHolder() {
        return customer.asBukkitPlayer();
    }

    public @NotNull Collection<Payment> getPayments() {
        ForeignCollection<Payment> payments = customer.getPayments();
        return payments != null && !payments.isEmpty()
                ? Collections.unmodifiableCollection(payments)
                : Collections.emptyList();
    }

    public @NotNull Collection<Payment> getShopCartPayments() {
        return getPayments().stream()
                .filter(Payment::isReported)
                .filter(Payment::isUncollected)
                .collect(Collectors.toList());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ShopCart shopCart = (ShopCart) o;
        return Objects.equals(customer, shopCart.customer);
    }

    @Override
    public int hashCode() {
        return Objects.hash(customer);
    }

    @Override
    public @NotNull String toString() {
        return "ShopCart{" +
                "customer=" + customer +
                '}';
    }

}
