package ru.easydonate.easypayments.database.model;

import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Getter
@NoArgsConstructor
@DatabaseTable(tableName = "easypayments_customers")
public final class Customer {

    public static final String COLUMN_PLAYER_NAME = "player_name";
    public static final String COLUMN_PLAYER_UUID = "player_uuid";
    public static final String COLUMN_CREATED_AT = "created_at";
    public static final String COLUMN_UPDATED_AT = "updated_at";

    @DatabaseField(columnName = COLUMN_PLAYER_NAME, id = true)
    private String playerName;

    @DatabaseField(columnName = COLUMN_PLAYER_UUID, canBeNull = false)
    private UUID playerUUID;

    @DatabaseField(columnName = COLUMN_CREATED_AT, canBeNull = false)
    private LocalDateTime createdAt;

    @DatabaseField(columnName = COLUMN_UPDATED_AT, canBeNull = false)
    private LocalDateTime updatedAt;

    @ForeignCollectionField
    private ForeignCollection<Payment> payments;

    public Customer(@NotNull OfflinePlayer bukkitPlayer) {
        this(bukkitPlayer.getName(), bukkitPlayer.getUniqueId());
    }

    public Customer(@NotNull String playerName, @NotNull UUID playerUUID) {
        this.playerName = playerName;
        this.playerUUID = playerUUID;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = createdAt;
    }

    public @NotNull Payment createPayment(int paymentId, int serverId) {
        return new Payment(this, paymentId, serverId);
    }

    public @NotNull OfflinePlayer asBukkitPlayer() {
        return Bukkit.getOfflinePlayer(playerUUID);
    }

    public @NotNull Player asOnlinePlayer() {
        return Bukkit.getPlayer(playerUUID);
    }

    public int getPaymentsAmount() {
        return payments != null ? payments.size() : 0;
    }

    public void updateUUID(@NotNull UUID uuid) {
        this.playerUUID = uuid;
        onUpdate();
    }

    private void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Customer customer = (Customer) o;
        return Objects.equals(playerName, customer.playerName) &&
                Objects.equals(playerUUID, customer.playerUUID) &&
                Objects.equals(createdAt, customer.createdAt) &&
                Objects.equals(updatedAt, customer.updatedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(playerName, playerUUID, createdAt, updatedAt);
    }

    @Override
    public @NotNull String toString() {
        return "Customer{" +
                "playerName='" + playerName + '\'' +
                ", playerUUID=" + playerUUID +
                ", payments=" + getPaymentsAmount() +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }

}
