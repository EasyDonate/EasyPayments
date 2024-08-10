package ru.easydonate.easypayments.core.util;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public final class Pair<K, V> {

    private final K key;
    private final V value;

}
