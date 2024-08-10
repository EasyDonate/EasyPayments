package ru.easydonate.easypayments.database.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.easydonate.easypayments.database.persister.JsonArrayPersister;
import ru.easydonate.easypayments.core.easydonate4j.extension.data.model.object.CommandReport;
import ru.easydonate.easypayments.core.easydonate4j.longpoll.data.model.object.PurchasedProduct;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Getter
@NoArgsConstructor
@DatabaseTable(tableName = "easypayments_purchases")
public final class Purchase {

    public static final String COLUMN_ID = "id";
    public static final String COLUMN_PAYMENT_ID = "payment_id";
    public static final String COLUMN_PRODUCT_ID = "product_id";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_AMOUNT = "amount";
    public static final String COLUMN_COST = "cost";
    public static final String COLUMN_COMMANDS = "commands";
    public static final String COLUMN_RESPONSES = "responses";
    public static final String COLUMN_CREATED_AT = "created_at";
    public static final String COLUMN_UPDATED_AT = "updated_at";

    @DatabaseField(columnName = COLUMN_ID, generatedId = true)
    private int id;

    @DatabaseField(columnName = COLUMN_PAYMENT_ID, foreign = true, canBeNull = false)
    private Payment payment;

    @DatabaseField(columnName = COLUMN_PRODUCT_ID, canBeNull = false)
    private int productId;

    @DatabaseField(columnName = COLUMN_NAME, canBeNull = false)
    private String name;

    @DatabaseField(columnName = COLUMN_AMOUNT, canBeNull = false)
    private int amount;

    @DatabaseField(columnName = COLUMN_COST, canBeNull = false)
    private double cost;

    @DatabaseField(columnName = COLUMN_COMMANDS, persisterClass = JsonArrayPersister.class)
    private List<String> commands;

    @DatabaseField(columnName = COLUMN_RESPONSES, persisterClass = JsonArrayPersister.class)
    private List<String> responses;

    @DatabaseField(columnName = COLUMN_CREATED_AT, canBeNull = false)
    private LocalDateTime createdAt;

    @DatabaseField(columnName = COLUMN_UPDATED_AT, canBeNull = false)
    private LocalDateTime updatedAt;

    public Purchase(@NotNull Payment payment, int productId, @NotNull String name, int amount, double cost, @Nullable List<String> commands) {
        this.payment = payment;
        this.productId = productId;
        this.name = name;
        this.amount = amount;
        this.cost = cost;
        this.commands = commands;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = createdAt;
    }

    public static @NotNull Purchase create(@NotNull Payment payment, @NotNull PurchasedProduct product) {
        return new Purchase(
                payment,
                product.getId(),
                product.getName(),
                product.getCount(),
                product.getCost(),
                product.getRawCommands()
        );
    }

    public static @NotNull Purchase create(@NotNull Payment payment, @NotNull PurchasedProduct product, @NotNull List<CommandReport> commandReports) {
        Purchase purchase = create(payment, product);
        purchase.collect(commandReports);
        return purchase;
    }

    public @NotNull List<CommandReport> constructCommandReports() {
        if(commands == null || responses == null)
            return Collections.emptyList();

        List<CommandReport> commandReports = new ArrayList<>();
        for(int i = 0; i < commands.size(); i++) {
            String command = commands.get(i);
            String response = getResponse(i, "");
            commandReports.add(CommandReport.create(command, response));
        }

        return commandReports;
    }

    public @Nullable String getResponse(int index, @Nullable String defaultValue) {
        if(responses == null || index >= responses.size())
            return defaultValue;
        else
            return responses.get(index);
    }

    public boolean hasCommands() {
        return commands != null && !commands.isEmpty();
    }

    public boolean isCollected() {
        return responses != null;
    }

    public boolean collect(@NotNull List<CommandReport> commandReports) {
        if(isCollected())
             return false;

        this.commands = commandReports.stream().map(CommandReport::getCommand).collect(Collectors.toList());
        this.responses = commandReports.stream().map(CommandReport::getResponse).collect(Collectors.toList());
        this.updatedAt = LocalDateTime.now();
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Purchase purchase = (Purchase) o;
        return id == purchase.id &&
                productId == purchase.productId &&
                amount == purchase.amount &&
                Double.compare(purchase.cost, cost) == 0 &&
                Objects.equals(payment, purchase.payment) &&
                Objects.equals(name, purchase.name) &&
                Objects.equals(commands, purchase.commands) &&
                Objects.equals(responses, purchase.responses) &&
                Objects.equals(createdAt, purchase.createdAt) &&
                Objects.equals(updatedAt, purchase.updatedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                id, payment, productId, name, amount, cost,
                commands, responses, createdAt, updatedAt
        );
    }

    @Override
    public @NotNull String toString() {
        return "Purchase{" +
                "id=" + id +
                ", payment=" + payment +
                ", productId=" + productId +
                ", name='" + name + '\'' +
                ", amount=" + amount +
                ", cost=" + cost +
                ", commands=" + commands +
                ", responses=" + responses +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }

}