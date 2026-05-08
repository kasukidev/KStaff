package me.kasuki.kstaff.api.database.redis.repository.hash;

import me.kasuki.kstaff.api.constant.KStaffConstant;
import me.kasuki.kstaff.api.database.redis.AbstractRedisHandler;

import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Base repository implementation for redis hash operations.
 */
public abstract class AbstractRedisHashRepository {

    /**
     * Stores redis handler.
     */
    private final AbstractRedisHandler redisHandler;

    /**
     * Creates a new AbstractRedisHashRepository instance.
     */
    protected AbstractRedisHashRepository(AbstractRedisHandler redisHandler) {
        this.redisHandler = redisHandler;
    }

    /**
     * Executes save hash to redis.
     */
    public void saveHashToRedis(String hashKey, Map<byte[], byte[]> map) {
        this.redisHandler.runCommand(
                jedis -> jedis.hset(hashKey.getBytes(KStaffConstant.CHARSET), map));
    }

    /**
     * Executes remove hash from redis.
     */
    public void removeHashFromRedis(String hashKey) {
        this.redisHandler.runCommand(jedis -> jedis.del(hashKey.getBytes(KStaffConstant.CHARSET)));
    }

    /**
     * Gets hash from redis.
     */
    public void getHashFromRedis(String hashKey, Consumer<Optional<Map<byte[], byte[]>>> consumer) {
        this.redisHandler.runCommand(
                jedis ->
                        consumer.accept(Optional.of(jedis.hgetAll(hashKey.getBytes(KStaffConstant.CHARSET)))));
    }

    /**
     * Executes save hash field to redis.
     */
    public void saveHashFieldToRedis(String hashKey, String fieldKey, byte[] fieldValue) {
        this.redisHandler.runCommand(
                jedis ->
                        jedis.hset(
                                hashKey.getBytes(KStaffConstant.CHARSET),
                                fieldKey.getBytes(KStaffConstant.CHARSET),
                                fieldValue));
    }

    /**
     * Executes remove hash field from redis.
     */
    public void removeHashFieldFromRedis(String hashKey, String fieldKey) {
        this.redisHandler.runCommand(
                jedis ->
                        jedis.hdel(
                                hashKey.getBytes(KStaffConstant.CHARSET),
                                fieldKey.getBytes(KStaffConstant.CHARSET)));
    }

    /**
     * Gets hash field from redis.
     */
    public void getHashFieldFromRedis(
            String hashKey, String fieldKey, Consumer<Optional<byte[]>> consumer) {
        this.redisHandler.runCommand(
                jedis ->
                        consumer.accept(
                                Optional.ofNullable(
                                        jedis.hget(
                                                hashKey.getBytes(KStaffConstant.CHARSET),
                                                fieldKey.getBytes(KStaffConstant.CHARSET)))));
    }
}
