package ru.easydonate.easypayments.core.easydonate4j.longpoll.data.model.object;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import ru.easydonate.easypayments.core.easydonate4j.longpoll.data.model.EventObject;
import ru.easydonate.easypayments.core.exception.StructureValidationException;

import java.util.List;
import java.util.Objects;

@Getter
public final class NewWithdrawEvent extends EventObject {

    @SerializedName("withdraw_id")
    private int withdrawId;

    @SerializedName("commands")
    private List<String> commands;

    @Override
    public void validate() throws StructureValidationException {
        if (withdrawId <= 0)
            validationFail("'withdrawId' must be > 0, but it's %d", withdrawId);

        if (commands == null || commands.isEmpty())
            validationFail("no commands present");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        NewWithdrawEvent that = (NewWithdrawEvent) o;
        return withdrawId == that.withdrawId &&
                Objects.equals(commands, that.commands);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), withdrawId, commands);
    }

    @Override
    public @NotNull String toString() {
        return "NewWithdrawEvent{" +
                "customer='" + customer + '\'' +
                ", pluginEvents=" + pluginEvents +
                ", withdrawId=" + withdrawId +
                ", commands=" + commands +
                '}';
    }

}
