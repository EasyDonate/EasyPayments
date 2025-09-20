package ru.easydonate.easypayments.core.easydonate4j.extension.data.model;

import com.google.gson.annotations.SerializedName;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class PluginStateModel {

    public static final @NotNull PluginStateModel DEFAULT = new PluginStateModel(Modes.DEFAULT);

    @SerializedName("modes")
    private Modes modes;

    public boolean isPlayersSyncActive() {
        return modes != null && modes.isPlayersSyncActive();
    }

    public boolean isPurchaseIssuanceActive() {
        return modes != null && modes.isPurchaseIssuanceActive();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass())
            return false;

        PluginStateModel that = (PluginStateModel) o;
        return Objects.equals(modes, that.modes);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(modes);
    }

    @Override
    public String toString() {
        return "PluginState{" +
                "modes=" + modes +
                '}';
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class Modes {

        public static final @NotNull Modes DEFAULT = new Modes(null, null);

        @SerializedName("issue_purchases")
        private Boolean issuePurchases;

        @SerializedName("sync_players")
        private Boolean syncPlayers;

        private boolean isPlayersSyncActive() {
            return syncPlayers != null && syncPlayers;
        }

        private boolean isPurchaseIssuanceActive() {
            return issuePurchases != null && issuePurchases;
        }

        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass())
                return false;

            Modes that = (Modes) o;
            return Objects.equals(issuePurchases, that.issuePurchases)
                    && Objects.equals(syncPlayers, that.syncPlayers);
        }

        @Override
        public int hashCode() {
            return Objects.hash(issuePurchases, syncPlayers);
        }

        @Override
        public String toString() {
            return "Modes{" +
                    "issuePurchases=" + issuePurchases +
                    ", syncPlayers=" + syncPlayers +
                    '}';
        }

    }

}
