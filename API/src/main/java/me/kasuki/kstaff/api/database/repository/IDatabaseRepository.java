package me.kasuki.kstaff.api.database.repository;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Defines a generic database repository abstraction for key-value storage.
 *
 * <p>This interface provides both synchronous and asynchronous access methods for storing and
 * retrieving raw binary data ({@code byte[]}) associated with keys.
 *
 * <p>Implementations may wrap various persistence mechanisms such as relational databases, embedded
 * stores (e.g., SQLite), or distributed systems.
 *
 * <p>Thread-safety and execution model (e.g., whether async methods execute on dedicated worker
 * threads or event loops) are implementation-dependent and should be clearly documented.
 *
 * @param <K> the key type used to identify database entries
 */
public interface IDatabaseRepository<K> extends AutoCloseable {

    /**
     * Saves or replaces a single entry in the database.
     *
     * @param key   the key identifying the entry
     * @param value the binary value to store
     */
    void saveToDatabase(K key, byte[] value);

    /**
     * Saves or replaces multiple entries in a batch operation.
     *
     * <p>Implementations should attempt to perform this operation atomically where supported by the
     * underlying database.
     *
     * @param entries a map of keys to binary values
     */
    void saveBatch(Map<K, byte[]> entries);

    /**
     * Deletes multiple entries in a batch operation.
     *
     * <p>Implementations should attempt to perform this operation atomically where supported.
     *
     * @param keys the collection of keys to remove
     */
    void deleteBatch(Collection<K> keys);

    /**
     * Retrieves a single entry synchronously.
     *
     * <p>This method blocks until the database operation completes.
     *
     * @param key the key identifying the entry
     * @return an {@link Optional} containing the stored value if present
     */
    Optional<byte[]> getFromDatabaseSync(K key);

    /**
     * Retrieves all entries synchronously.
     *
     * <p>This method blocks until all entries are loaded.
     *
     * @return a collection of all stored values
     */
    Collection<byte[]> getAllEntriesFromDatabaseSync();

    /**
     * Retrieves a single entry asynchronously.
     *
     * <p>The provided {@link Consumer} will be invoked when the operation completes. The execution
     * context of the consumer is implementation-defined.
     *
     * @param key      the key identifying the entry
     * @param consumer the callback that receives the result
     */
    void getFromDatabase(K key, Consumer<Optional<byte[]>> consumer);

    /**
     * Retrieves all entries asynchronously.
     *
     * <p>The provided {@link Consumer} will be invoked when the operation completes. The execution
     * context is implementation-defined.
     *
     * @param consumer the callback that receives the collection of values
     */
    void getAllEntriesFromDatabase(Consumer<Collection<byte[]>> consumer);

    /**
     * Removes a single entry from the database.
     *
     * @param key the key identifying the entry to remove
     */
    void removeFromDatabase(K key);

    /**
     * Closes the repository and releases underlying resources.
     *
     * <p>After calling this method, further operations may throw exceptions.
     *
     * @throws Exception if an error occurs while closing the repository
     */
    @Override
    void close() throws Exception;
}
