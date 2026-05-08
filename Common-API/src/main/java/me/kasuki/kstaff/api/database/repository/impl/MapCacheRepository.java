package me.kasuki.kstaff.api.database.repository.impl;

import me.kasuki.kstaff.api.database.repository.ICacheRepository;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Repository implementation for map cache operations.
 */
public class MapCacheRepository<K, V> implements ICacheRepository<K, V> {

    /**
     * Stores this field value.
     */
    private final Map<K, V> cache = new ConcurrentHashMap<>();

    /**
     * Executes add to cache.
     */
    @Override
    public void addToCache(K key, V value) {
        this.cache.put(key, value);
    }

    /**
     * Executes remove from cache.
     */
    @Override
    public void removeFromCache(K key) {
        this.cache.remove(key);
    }

    /**
     * Gets from cache.
     */
    @Override
    public Optional<V> getFromCache(K key) {
        return Optional.ofNullable(this.cache.get(key));
    }

    /**
     * Gets all values from cache.
     */
    @Override
    public Collection<V> getAllValuesFromCache() {
        return this.cache.values();
    }

    /**
     * Executes exists in cache.
     */
    @Override
    public boolean existsInCache(K key) {
        return this.cache.containsKey(key);
    }
}
