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
    public static final String COLUMN_IS_RECEIVED = "is_received";
    public static final String COLUMN_PAYMENT_ID = "payment_id";
    public static final String COLUMN_COMMANDS = "commands";
    public static final String COLUMN_RESPONSES = "responses";
    public static final String COLUMN_CREATED_AT = "created_at";
    public static final String COLUMN_UPDATED_AT = "updated_at";

    @DatabaseField(columnName = COLUMN_ID, generatedId = true)
    private int id;

    @DatabaseField(columnName = COLUMN_CUSTOMER_ID, foreign = true, canBeNull = false)
    private Customer customer;

    @DatabaseField(columnName = COLUMN_IS_RECEIVED, canBeNull = false)
    private boolean received;

    @DatabaseField(columnName = COLUMN_PAYMENT_ID, canBeNull = false)
    private int paymentId;

    @DatabaseField(columnName = COLUMN_COMMANDS, dataType = DataType.LONG_STRING, persisterClass = JsonArrayPersister.class)
    private List<String> commands;

    @DatabaseField(columnName = COLUMN_RESPONSES, dataType = DataType.LONG_STRING, persisterClass = JsonArrayPersister.class)
    private List<String> responses;

    @DatabaseField(columnName = COLUMN_CREATED_AT, canBeNull = false)
    private LocalDateTime created_at;

    @DatabaseField(columnName = COLUMN_UPDATED_AT, canBeNull = false)
    private LocalDateTime updated_at;

    public Purchase(@NotNull Customer customer, int paymentId, @Nullable List<String> commands) {
        this.customer = customer;
        this.paymentId = paymentId;
        this.commands = commands;
        this.created_at = LocalDateTime.now();
        this.updated_at = created_at;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Purchase purchase = (Purchase) o;
        return id == purchase.id &&
                received == purchase.received &&
                paymentId == purchase.paymentId &&
                Objects.equals(commands, purchase.commands) &&
                Objects.equals(responses, purchase.responses) &&
                Objects.equals(created_at, purchase.created_at) &&
                Objects.equals(updated_at, purchase.updated_at);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, received, paymentId, commands, responses, created_at, updated_at);
    }

    @Override
    public @NotNull String toString() {
        return "Purchase{" +
                "id=" + id +
                ", customer=" + customer.getPlayerName() +
                ", received=" + received +
                ", paymentId=" + paymentId +
                ", commands=" + commands +
                ", responses=" + responses +
                ", created_at=" + created_at +
                ", updated_at=" + updated_at +
                '}';
    }

}