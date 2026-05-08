package me.kasuki.kstaff.api.database.sqlite.impl;

import com.github.benmanes.caffeine.cache.Cache;
import lombok.Getter;
import me.kasuki.kstaff.api.database.repository.ICacheRepository;
import me.kasuki.kstaff.api.database.repository.impl.CaffieneCacheRepository;
import me.kasuki.kstaff.api.database.sqlite.AbstractSQLiteRepository;

import java.io.File;
import java.util.Collection;
import java.util.Optional;

/**
 * Base repository implementation for sq lite cache operations.
 */
@Getter
public abstract class AbstractSQLiteCacheRepository<K, V> extends AbstractSQLiteRepository<K>
        implements ICacheRepository<K, V> {

    /**
     * Stores this field value.
     */
    private final CaffieneCacheRepository<K, V> cacheRepository;

    /**
     * Creates a new AbstractSQLiteCacheRepository instance.
     */
    protected AbstractSQLiteCacheRepository(
            File directory, String name, String tableName, Cache<K, V> cache) {
        super(directory, name, tableName);
        this.cacheRepository = new CaffieneCacheRepository<>(cache);
    }

    /**
     * Creates a new AbstractSQLiteCacheRepository instance.
     */
    protected AbstractSQLiteCacheRepository(File directory, String name, String tableName) {
        super(directory, name, tableName);
        this.cacheRepository = new CaffieneCacheRepository<>();
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
