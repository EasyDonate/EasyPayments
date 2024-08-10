package ru.easydonate.easypayments.core.placeholder.bean;

import lombok.AllArgsConstructor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.easydonate.easypayments.core.placeholder.PlaceholderReplacerBus;
import ru.easydonate.easypayments.core.placeholder.StringContainer;

@AllArgsConstructor
final class PlaceholderReplacingBean<T> implements PlaceholderSupportingBean<T> {

    private final StringContainer stringContainer;
    private final BeanValueConverter<T> valueConverter;

    @Override
    public @Nullable T get(@NotNull PlaceholderReplacerBus replacerBus, @NotNull Player bukkitPlayer) {
        StringContainer processed = stringContainer.copy().processPlaceholders(replacerBus, bukkitPlayer);

        String processedString = processed.get();
        if(processedString == null || processedString.isEmpty())
            return null;

        try {
            return valueConverter.convert(processedString);
        } catch (Exception ignored) {
            return null;
        }
    }

    @Override
    public @NotNull String toString() {
        return "PlaceholderReplacingBean{" +
                "container=" + stringContainer +
                '}';
    }

}
