package ru.easydonate.easypayments.event.incoming;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import ru.easydonate.easypayments.event.AsyncBukkitEvent;

import java.util.List;

@Getter
@AllArgsConstructor
public final class IncomingRewardCollectingEvent extends AsyncBukkitEvent {

    private final int paymentId;
    private final @NotNull List<String> commands;

    @Override
    public @NotNull String toString() {
        return "IncomingRewardCollectingEvent{" +
                "paymentId=" + paymentId +
                ", commands=" + commands +
                '}';
    }

}
