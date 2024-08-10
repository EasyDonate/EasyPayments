package ru.easydonate.easypayments.core.placeholder.bean;

import lombok.AllArgsConstructor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.easydonate.easypayments.core.placeholder.PlaceholderReplacerBus;

@AllArgsConstructor
final class PlaceholderConstantBean<T> implements PlaceholderSupportingBean<T> {

    private final T constantValue;

    @Override
    public @Nullable T get(@NotNull PlaceholderReplacerBus replacerBus, @NotNull Player bukkitPlayer) {
        return constantValue;
    }

    @Override
    public @NotNull String toString() {
        return "PlaceholderConstantBean{" +
                "value=" + constantValue +
                '}';
    }

}
