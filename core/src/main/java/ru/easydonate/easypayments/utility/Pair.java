package ru.easydonate.easypayments.utility;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

@Getter
@AllArgsConstructor
public final class Pair<K, V> {

    private final K key;
    private final V value;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Pair<?, ?> pair = (Pair<?, ?>) o;
        return Objects.equals(key, pair.key) &&
                Objects.equals(value, pair.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, value);
    }

    @Override
    public @NotNull String toString() {
        return "Pair{" +
                "key=" + key +
                ", value=" + value +
                '}';
    }

}
