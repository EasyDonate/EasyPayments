package ru.easydonate.easypayments.core.easydonate4j.extension.data.model;

import com.google.gson.annotations.SerializedName;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class PluginStateModel {

    public static final @NotNull PluginStateModel DEFAULT = new PluginStateModel(null, null);

    @SerializedName("issue_purchases")
    private Boolean issuePurchases;

    @SerializedName("sync_players")
    private Boolean syncPlayers;

    public boolean isPlayersSyncActive() {
        return syncPlayers != null && syncPlayers;
    }

    public boolean isPurchaseIssuanceActive() {
        return issuePurchases != null && issuePurchases;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass())
            return false;

        PluginStateModel that = (PluginStateModel) o;
        return Objects.equals(issuePurchases, that.issuePurchases)
                && Objects.equals(syncPlayers, that.syncPlayers);
    }

    @Override
    public int hashCode() {
        return Objects.hash(issuePurchases, syncPlayers);
    }

    @Override
    public String toString() {
        return "PluginStateResponse{" +
                "issuePurchases=" + issuePurchases +
                ", syncPlayers=" + syncPlayers +
                '}';
    }

}
