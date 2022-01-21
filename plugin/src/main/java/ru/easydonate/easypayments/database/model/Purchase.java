package ru.easydonate.easypayments.database.model;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.easydonate.easypayments.database.persister.JsonArrayPersister;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Getter
@NoArgsConstructor
@DatabaseTable(tableName = "easypayments_purchases")
public final class Purchase {

    public static final String COLUMN_ID = "id";
    public static final String COLUMN_CUSTOMER_ID = "customer_id";
    public static final String COLUMN_COLLECTED_AT = "collected_at";
    public static final String COLUMN_REPORTED_AT = "reported_at";
    public static final String COLUMN_PAYMENT_ID = "payment_id";
    public static final String COLUMN_COMMANDS = "commands";
    public static final String COLUMN_RESPONSES = "responses";
    public static final String COLUMN_CREATED_AT = "created_at";
    public static final String COLUMN_UPDATED_AT = "updated_at";

    @DatabaseField(columnName = COLUMN_ID, generatedId = true)
    private int id;

    @DatabaseField(columnName = COLUMN_CUSTOMER_ID, foreign = true, canBeNull = false)
    private Customer customer;

    @DatabaseField(columnName = COLUMN_COLLECTED_AT)
    private LocalDateTime collectedAt;

    @DatabaseField(columnName = COLUMN_REPORTED_AT)
    private LocalDateTime reportedAt;

    @DatabaseField(columnName = COLUMN_PAYMENT_ID, canBeNull = false)
    private int paymentId;

    @DatabaseField(columnName = COLUMN_COMMANDS, dataType = DataType.LONG_STRING, persisterClass = JsonArrayPersister.class)
    private List<String> commands;

    @DatabaseField(columnName = COLUMN_RESPONSES, dataType = DataType.LONG_STRING, persisterClass = JsonArrayPersister.class)
    private List<String> responses;

    @DatabaseField(columnName = COLUMN_CREATED_AT, canBeNull = false)
    private LocalDateTime createdAt;

    @DatabaseField(columnName = COLUMN_UPDATED_AT, canBeNull = false)
    private LocalDateTime updatedAt;

    public Purchase(@NotNull Customer customer, int paymentId, @Nullable List<String> commands) {
        this.customer = customer;
        this.paymentId = paymentId;
        this.commands = commands;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = createdAt;
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
        if(isCollected())
            return false;

        this.collectedAt = LocalDateTime.now();
        this.updatedAt = collectedAt;
        return true;
    }

    public boolean markAsReported() {
        if(isReported())
            return false;

        this.reportedAt = LocalDateTime.now();
        this.updatedAt = reportedAt;
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Purchase purchase = (Purchase) o;
        return id == purchase.id &&
                paymentId == purchase.paymentId &&
                Objects.equals(collectedAt, purchase.collectedAt) &&
                Objects.equals(reportedAt, purchase.reportedAt) &&
                Objects.equals(commands, purchase.commands) &&
                Objects.equals(responses, purchase.responses) &&
                Objects.equals(createdAt, purchase.createdAt) &&
                Objects.equals(updatedAt, purchase.updatedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                id, collectedAt, reportedAt, paymentId,
                commands, responses, createdAt, updatedAt
        );
    }

    @Override
    public @NotNull String toString() {
        return "Purchase{" +
                "id=" + id +
                ", customer=" + customer +
                ", collectedAt=" + collectedAt +
                ", reportedAt=" + reportedAt +
                ", paymentId=" + paymentId +
                ", commands=" + commands +
                ", responses=" + responses +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }

}