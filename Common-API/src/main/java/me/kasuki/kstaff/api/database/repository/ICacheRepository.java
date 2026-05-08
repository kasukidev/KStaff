package me.kasuki.kstaff.api.database.repository;

import java.util.Collection;
import java.util.Optional;

/**
 * Defines a generic in-memory cache repository abstraction.
 *
 * <p>This interface provides basic cache operations for storing, retrieving, and removing key-value
 * pairs. Implementations may choose any underlying data structure (e.g., {@code ConcurrentHashMap},
 * Caffeine, Redis-backed cache, etc.).
 *
 * <p>Thread-safety is implementation-dependent. Implementations should clearly document whether
 * they are safe for concurrent access.
 *
 * @param <K> the key type
 * @param <V> the value type
 */
public interface ICacheRepository<K, V> {

    /**
     * Adds or replaces a value in the cache.
     *
     * @param key   the key under which the value is stored
     * @param value the value to cache
     */
    void addToCache(K key, V value);

    /**
     * Removes a value from the cache by key.
     *
     * @param key the key associated with the value to remove
     */
    void removeFromCache(K key);

    /**
     * Retrieves a value from the cache.
     *
     * @param key the key associated with the value
     * @return an {@link Optional} containing the cached value if present
     */
    Optional<V> getFromCache(K key);

    /**
     * Returns all cached values.
     *
     * <p>The returned collection may be a snapshot or a live view depending on the implementation.
     *
     * @return a collection of all cached values
     */
    Collection<V> getAllValuesFromCache();

    /**
     * Determines whether a key exists in the cache.
     *
     * @param key the key to check
     * @return {@code true} if the key exists in the cache; {@code false} otherwise
     */
    boolean existsInCache(K key);
}
