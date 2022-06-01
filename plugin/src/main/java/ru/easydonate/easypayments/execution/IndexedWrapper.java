package ru.easydonate.easypayments.execution;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.Function;

@Getter
public class IndexedWrapper<T> implements Comparable<IndexedWrapper<T>> {

    protected final int index;
    protected T object;

    public IndexedWrapper(int index) {
        this(index, null);
    }

    public IndexedWrapper(int index, @Nullable T object) {
        this.index = index;
        this.object = object;
    }
    
    public <O> @NotNull IndexedWrapper<O> map(@NotNull Function<T, O> mapper) {
        O mappedObject = object != null ? mapper.apply(object) : null;
        return new IndexedWrapper<>(index, mappedObject);
    }

    public @NotNull IndexedWrapper<T> setObject(@Nullable T object) {
        this.object = object;
        return this;
    }

    @Override
    public int compareTo(@NotNull IndexedWrapper<T> o) {
        return Integer.compare(index, o.index);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        IndexedWrapper<?> that = (IndexedWrapper<?>) o;
        return index == that.index &&
                Objects.equals(object, that.object);
    }

    @Override
    public int hashCode() {
        return Objects.hash(index, object);
    }

    @Override
    public @NotNull String toString() {
        return "IndexedWrapper{" +
                "index=" + index +
                ", object=" + object +
                '}';
    }

}
