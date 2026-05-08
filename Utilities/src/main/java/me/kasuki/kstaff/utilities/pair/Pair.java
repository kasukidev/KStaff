package me.kasuki.kstaff.utilities.pair;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Represents a generic pair of values.
 */
@RequiredArgsConstructor
@Getter
public class Pair<K, V> {

    /**
     * Stores key.
     */
    private final K key;

    /**
     * Stores value.
     */
    private final V value;

    /**
     * Executes from.
     */
    public static <K, V> Pair<K, V> from(K key, V value) {
        return new Pair<>(key, value);
    }
}
