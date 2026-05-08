package me.kasuki.kstaff.api.database.sqlite.impl;

import me.kasuki.kstaff.api.database.repository.ICacheRepository;
import me.kasuki.kstaff.api.database.repository.impl.MapCacheRepository;
import me.kasuki.kstaff.api.database.sqlite.AbstractSQLiteRepository;

import java.io.File;
import java.util.Collection;
import java.util.Optional;

/**
 * Base repository implementation for sq lite map operations.
 */
public abstract class AbstractSQLiteMapRepository<K, V> extends AbstractSQLiteRepository<K>
        implements ICacheRepository<K, V> {
    /**
     * Stores this field value.
     */
    private final MapCacheRepository<K, V> cacheRepository;

    /**
     * Creates a new AbstractSQLiteMapRepository instance.
     */
    protected AbstractSQLiteMapRepository(File directory, String name, String tableName) {
        super(directory, name, tableName);
        this.cacheRepository = new MapCacheRepository<>();
    }

    /**
     * Executes add to cache.
     */
    @Override
    public void addToCache(K key, V value) {
        this.cacheRepository.addToCache(key, value);
    }

    /**
     * Executes remove from cache.
     */
    @Override
    public void removeFromCache(K key) {
        this.cacheRepository.removeFromCache(key);
    }

    /**
     * Gets from cache.
     */
    @Override
    public Optional<V> getFromCache(K key) {
        return this.cacheRepository.getFromCache(key);
    }

    /**
     * Executes exists in cache.
     */
    @Override
    public boolean existsInCache(K key) {
        return this.cacheRepository.existsInCache(key);
    }

    /**
     * Gets all values from cache.
     */
    @Override
    public Collection<V> getAllValuesFromCache() {
        return this.cacheRepository.getAllValuesFromCache();
    }
}
