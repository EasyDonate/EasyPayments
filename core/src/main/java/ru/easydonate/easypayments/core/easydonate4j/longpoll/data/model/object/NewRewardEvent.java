package ru.easydonate.easypayments.core.easydonate4j.longpoll.data.model.object;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import ru.easydonate.easypayments.core.easydonate4j.longpoll.data.model.EventObject;
import ru.easydonate.easypayments.core.exception.StructureValidationException;

import java.util.List;
import java.util.Objects;

@Getter
public final class NewRewardEvent extends EventObject {

    @SerializedName("reward_id")
    private int rewardId;

    @SerializedName("commands")
    private List<String> commands;

    @Override
    public void validate() throws StructureValidationException {
        super.validate();

        if (rewardId <= 0)
            validationFail("'rewardId' must be > 0, but it's %d", rewardId);

        if (commands == null || commands.isEmpty())
            validationFail("no commands present");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        NewRewardEvent that = (NewRewardEvent) o;
        return rewardId == that.rewardId &&
                Objects.equals(commands, that.commands);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), rewardId, commands);
    }

    @Override
    public @NotNull String toString() {
        return "NewRewardEvent{" +
                "customer='" + customer + '\'' +
                ", createdAt=" + createdAt +
                ", pluginEvents=" + pluginEvents +
                ", rewardId=" + rewardId +
                ", commands=" + commands +
                '}';
    }

}
