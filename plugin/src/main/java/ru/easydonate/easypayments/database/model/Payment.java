package ru.easydonate.easypayments.database.model;

import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import ru.easydonate.easypayments.core.easydonate4j.extension.data.model.object.CommandReport;
import ru.easydonate.easypayments.core.easydonate4j.longpoll.data.model.object.PurchasedProduct;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Getter
@NoArgsConstructor
@DatabaseTable(tableName = "easypayments_payments")
public final class Payment {

    public static final String COLUMN_ID = "id";
    public static final String COLUMN_CUSTOMER_ID = "customer_id";
    public static final String COLUMN_SERVER_ID = "server_id";
    public static final String COLUMN_COLLECTED_AT = "collected_at";
    public static final String COLUMN_REPORTED_AT = "reported_at";
    public static final String COLUMN_CREATED_AT = "created_at";
    public static final String COLUMN_UPDATED_AT = "updated_at";

    @DatabaseField(columnName = COLUMN_ID, id = true)
    private int id;

    @DatabaseField(columnName = COLUMN_CUSTOMER_ID, foreign = true, canBeNull = false)
    private Customer customer;

    @DatabaseField(columnName = COLUMN_SERVER_ID, canBeNull = false)
    private int serverId;

    @DatabaseField(columnName = COLUMN_COLLECTED_AT)
    private LocalDateTime collectedAt;

    @DatabaseField(columnName = COLUMN_REPORTED_AT)
    private LocalDateTime reportedAt;

    @DatabaseField(columnName = COLUMN_CREATED_AT, canBeNull = false)
    private LocalDateTime createdAt;

    @DatabaseField(columnName = COLUMN_UPDATED_AT, canBeNull = false)
    private LocalDateTime updatedAt;

    @ForeignCollectionField
    private ForeignCollection<Purchase> purchases;

    public Payment(@NotNull Customer customer, int paymentId, int serverId) {
        this.id = paymentId;
        this.customer = customer;
        this.serverId = serverId;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = createdAt;
    }

    public @NotNull Purchase createPurchase(int productId, @NotNull String name, int amount, double cost, @NotNull List<String> commands) {
        return new Purchase(this, productId, name, amount, cost, commands);
    }

    public @NotNull Purchase createPurchase(@NotNull PurchasedProduct product) {
        return Purchase.create(this, product);
    }

    public @NotNull Purchase createPurchase(@NotNull PurchasedProduct product, @NotNull List<CommandReport> commandReports) {
        return Purchase.create(this, product, commandReports);
    }

    public int getPurchasesAmount() {
        return purchases != null ? purchases.size() : 0;
    }

    public boolean hasPurchases() {
        return getPurchasesAmount() != 0;
    }

    public boolean isCollected() {
        return collectedAt != null;
    }

    public boolean isUncollected() {
        return !isCollected();
    }

    public boolean isReported() {
        return reportedAt != null;
    }

    public boolean markAsCollected() {
        if (isCollected())
            return false;

        this.collectedAt = updatedAt = LocalDateTime.now();
        return true;
    }

    public boolean markAsReported() {
        if (isReported())
            return false;

        this.reportedAt = updatedAt = LocalDateTime.now();
        return true;
    }

    public void transfer(Customer customer) {
        this.customer = customer;
        this.updatedAt = LocalDateTime.now();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Payment payment = (Payment) o;
        return id == payment.id &&
                serverId == payment.serverId &&
                Objects.equals(customer, payment.customer) &&
                Objects.equals(collectedAt, payment.collectedAt) &&
                Objects.equals(reportedAt, payment.reportedAt) &&
                Objects.equals(createdAt, payment.createdAt) &&
                Objects.equals(updatedAt, payment.updatedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, customer, serverId, id, collectedAt, reportedAt, createdAt, updatedAt);
    }

    @Override
    public @NotNull String toString() {
        return "Payment{" +
                "id=" + id +
                ", customer=" + customer +
                ", serverId=" + serverId +
                ", purchases=" + getPurchasesAmount() +
                ", collectedAt=" + collectedAt +
                ", reportedAt=" + reportedAt +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }

}