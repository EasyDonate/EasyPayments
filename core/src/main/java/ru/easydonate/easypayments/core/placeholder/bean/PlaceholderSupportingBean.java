package ru.easydonate.easypayments.core.placeholder.bean;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.easydonate.easypayments.core.placeholder.PlaceholderReplacerBus;
import ru.easydonate.easypayments.core.placeholder.StringContainer;

@FunctionalInterface
public interface PlaceholderSupportingBean<T> {

    static <T> @NotNull PlaceholderSupportingBean<T> constant(@Nullable T constantValue) {
        return new PlaceholderConstantBean<>(constantValue);
    }

    static <T> @NotNull PlaceholderSupportingBean<T> placeholder(
            @Nullable String placeholderableString,
            @NotNull BeanValueConverter<T> valueConverter
    ) {
        return new PlaceholderReplacingBean<>(StringContainer.wrap(placeholderableString, true), valueConverter);
    }

    @Nullable T get(@NotNull PlaceholderReplacerBus replacerBus, @NotNull Player bukkitPlayer);

}
