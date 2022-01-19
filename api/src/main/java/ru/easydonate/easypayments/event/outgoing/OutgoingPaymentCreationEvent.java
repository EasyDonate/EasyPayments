package ru.easydonate.easypayments.event.outgoing;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import ru.easydonate.easypayments.event.AsyncBukkitEvent;

import java.util.List;

@Getter
@AllArgsConstructor
public final class OutgoingPaymentCreationEvent extends AsyncBukkitEvent {

    private final int paymentId;
    private final @NotNull List<String> commands;
    private final @NotNull List<String> responses;

    @Override
    public @NotNull String toString() {
        return "OutgoingPaymentCreationEvent{" +
                "paymentId=" + paymentId +
                ", commands=" + commands +
                '}';
    }

}
