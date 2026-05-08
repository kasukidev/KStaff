package me.kasuki.kstaff.api.database.repository.impl;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import me.kasuki.kstaff.api.database.repository.ICacheRepository;

import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * {@link ICacheRepository} implementation backed by Caffeine.
 *
 * <p>This implementation uses a {@link Cache} from the Caffeine library to provide
 * high-performance, thread-safe in-memory caching.
 *
 * <p>By default, entries expire 30 minutes after write. A custom {@link Cache} instance may be
 * supplied via constructor injection for advanced configuration (e.g., size limits, eviction
 * policies, statistics recording).
 *
 * <p>This class is thread-safe as long as the provided {@link Cache} instance is thread-safe (which
 * Caffeine caches are).
 *
 * @param <K> the key type
 * @param <V> the value type
 */
public class CaffieneCacheRepository<K, V> implements ICacheRepository<K, V> {

    /**
     * Underlying Caffeine cache instance.
     */
    private final Cache<K, V> cache;

    /**
     * Constructs a repository using a custom Caffeine cache instance.
     *
     * @param cache the cache implementation to use
     */
    public CaffieneCacheRepository(Cache<K, V> cache) {
        this.cache = cache;
    }

    /**
     * Constructs a repository with a default configuration.
     *
     * <p>The default configuration applies:
     *
     * <ul>
     *   <li>Expire entries 30 minutes after write
     * </ul>
     */
    public CaffieneCacheRepository() {
        this.cache = Caffeine.newBuilder().expireAfterWrite(30L, TimeUnit.MINUTES).build();
    }

    /**
     * Adds or replaces a value in the cache.
     *
     * @param key   the key under which the value is stored
     * @param value the value to cache
     */
    @Override
    public void addToCache(K key, V value) {
        this.cache.put(key, value);
    }

    /**
     * Removes a value from the cache.
     *
     * @param key the key associated with the value to remove
     */
    @Override
    public void removeFromCache(K key) {
        this.cache.invalidate(key);
    }

    /**
     * Retrieves a value from the cache.
     *
     * @param key the key associated with the value
     * @return an {@link Optional} containing the cached value if present
     */
    @Override
    public Optional<V> getFromCache(K key) {
        return Optional.ofNullable(this.cache.asMap().get(key));
    }

    /**
     * Determines whether a key exists in the cache.
     *
     * @param key the key to check
     * @return {@code true} if the key exists; {@code false} otherwise
     */
    @Override
    public boolean existsInCache(K key) {
        return this.cache.asMap().containsKey(key);
    }

    /**
     * Returns all cached values.
     *
     * <p>The returned collection is a live view of the cache values. Modifications to the cache will
     * be reflected in the collection.
     *
     * @return a collection of cached values
     */
    @Override
    public Collection<V> getAllValuesFromCache() {
        return this.cache.asMap().values();
    }
}
