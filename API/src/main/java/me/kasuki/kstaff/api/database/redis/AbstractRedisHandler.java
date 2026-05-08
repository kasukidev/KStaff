package me.kasuki.kstaff.api.database.redis;

import lombok.Getter;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * Base handler for redis operations.
 */
@Getter
public abstract class AbstractRedisHandler implements AutoCloseable {

    /**
     * Stores password.
     */
    private final String password;

    /**
     * Stores channel.
     */
    private final String channel;

    /**
     * Stores proxy channel.
     */
    private final String proxyChannel;

    /**
     * Stores jedis pool.
     */
    private final JedisPool jedisPool;

    /**
     * Stores subscription pool.
     */
    private final JedisPool subscriptionPool;

    /**
     * Creates a new AbstractRedisHandler instance.
     */
    protected AbstractRedisHandler(
            String host, int port, String password, String channel, String proxyChannel) {
        this.password = password;
        this.channel = channel;
        this.proxyChannel = proxyChannel;

        this.jedisPool = new JedisPool(host, port);
        this.subscriptionPool = new JedisPool(host, port);
    }

    /**
     * Executes run command.
     */
    public void runCommand(Consumer<Jedis> consumer) {
        CompletableFuture.runAsync(
                        () -> {
                            try (Jedis jedis = this.jedisPool.getResource()) {
                                if (!password.isEmpty()) {
                                    jedis.auth(password);
                                }

                                consumer.accept(jedis);
                            } catch (Exception exception) {
                                exception.printStackTrace();
                            }
                        })
                .exceptionally(
                        exception -> {
                            if (exception != null) {
                                exception.printStackTrace();
                            }
                            return null;
                        });
    }

    /**
     * Executes close.
     */
    @Override
    public void close() {
        if (this.jedisPool != null) {
            this.jedisPool.close();
        }
        if (this.subscriptionPool != null) {
            this.subscriptionPool.close();
        }
    }
}
