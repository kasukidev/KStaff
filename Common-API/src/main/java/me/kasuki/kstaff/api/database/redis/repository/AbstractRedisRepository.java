package me.kasuki.kstaff.api.database.redis.repository;

import lombok.Getter;
import me.kasuki.kstaff.api.database.redis.AbstractRedisHandler;
import redis.clients.jedis.params.ScanParams;
import redis.clients.jedis.resps.ScanResult;

import java.util.*;
import java.util.function.Consumer;

/**
 * Base repository implementation for redis operations.
 */
@Getter
public abstract class AbstractRedisRepository {

    /**
     * Stores redis handler.
     */
    private final AbstractRedisHandler redisHandler;

    /**
     * Constant for key separator.
     */
    public static final String KEY_SEPARATOR = ":";

    /**
     * Creates a new AbstractRedisRepository instance.
     */
    protected AbstractRedisRepository(AbstractRedisHandler redisHandler) {
        this.redisHandler = redisHandler;
    }

    /**
     * Executes add object.
     */
    public void addObject(String rootKey, String objectKey, byte[] value) {
        if (rootKey == null || rootKey.isEmpty()) {
            this.redisHandler.runCommand(jedis -> jedis.set(objectKey.getBytes(), value));
            return;
        }

        String finalKey = rootKey + KEY_SEPARATOR + objectKey;
        this.redisHandler.runCommand(jedis -> jedis.set(finalKey.getBytes(), value));
    }

    /**
     * Executes remove object.
     */
    public void removeObject(String rootKey, String objectKey) {
        if (rootKey == null || rootKey.isEmpty()) {
            this.redisHandler.runCommand(jedis -> jedis.del(objectKey.getBytes()));
            return;
        }

        String finalKey = rootKey + KEY_SEPARATOR + objectKey;
        this.redisHandler.runCommand(
                jedis -> {
                    jedis.del(finalKey.getBytes());
                });
    }

    /**
     * Gets object.
     */
    public void getObject(String rootKey, String objectKey, Consumer<Optional<byte[]>> consumer) {
        if (rootKey == null || rootKey.isEmpty()) {
            this.redisHandler.runCommand(
                    jedis -> consumer.accept(Optional.ofNullable(jedis.get(objectKey.getBytes()))));
            return;
        }

        String finalKey = rootKey + KEY_SEPARATOR + objectKey;
        this.redisHandler.runCommand(
                jedis -> consumer.accept(Optional.ofNullable(jedis.get(finalKey.getBytes()))));
    }

    /**
     * Gets objects.
     */
    public void getObjects(String rootKey, Consumer<List<byte[]>> consumer) {
        if (rootKey == null || rootKey.isEmpty()) {
            this.redisHandler.runCommand(jedis -> consumer.accept(Collections.emptyList()));
            return;
        }

        this.redisHandler.runCommand(
                jedis -> {
                    String finalKey = rootKey + KEY_SEPARATOR + "*";
                    byte[] cursor = ScanParams.SCAN_POINTER_START_BINARY;

                    ScanParams scanParams = new ScanParams().count(100).match(finalKey);
                    List<byte[]> bytes = new ArrayList<>();
                    do {
                        ScanResult<byte[]> scanResult = jedis.scan(cursor, scanParams);

                        for (byte[] objectKey : scanResult.getResult()) {
                            byte[] found = jedis.get(objectKey);
                            if (found != null) {
                                bytes.add(found);
                            }
                        }

                        cursor = scanResult.getCursorAsBytes();
                    } while (!Arrays.equals(cursor, ScanParams.SCAN_POINTER_START_BINARY));

                    consumer.accept(bytes);
                });
    }
}
