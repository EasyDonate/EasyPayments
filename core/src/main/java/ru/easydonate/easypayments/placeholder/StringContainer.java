package ru.easydonate.easypayments.placeholder;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class StringContainer {

    private String wrappedString;
    private final boolean supportingPlaceholders;

    public static @NotNull StringContainer wrap(@Nullable String wrappedString, boolean supportingPlaceholders) {
        return new StringContainer(wrappedString, supportingPlaceholders);
    }

    public @NotNull StringContainer copy() {
        return new StringContainer(wrappedString, supportingPlaceholders);
    }

    public @Nullable String get() {
        return wrappedString;
    }

    public boolean isEmpty() {
        return wrappedString == null || wrappedString.isEmpty();
    }

    public boolean isSupportingPlaceholders() {
        return supportingPlaceholders && !isEmpty();
    }

    public @NotNull StringContainer processPlaceholders(@NotNull PlaceholderReplacerBus replacerBus, @NotNull Player bukkitPlayer) {
        if(isEmpty())
            return this;

        return replacerBus.processPlaceholders(bukkitPlayer, this);
    }

    @Override
    public @NotNull String toString() {
        return "StringContainer{" +
                "string='" + wrappedString + '\'' +
                ", supportingPlaceholders=" + supportingPlaceholders +
                '}';
    }

}
