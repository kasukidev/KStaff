package me.kasuki.kstaff.api.database.redis.repository.stream.publisher;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.kasuki.kstaff.api.constant.KStaffConstant;
import me.kasuki.kstaff.api.database.redis.AbstractRedisHandler;
import me.kasuki.kstaff.api.database.redis.repository.stream.consumer.AbstractRedisStreamConsumer;
import ne.kasuki.kstaff.api.database.redis.event.EventOuterClass;
import redis.clients.jedis.StreamEntryID;
import redis.clients.jedis.params.XAddParams;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

/**
 * Base implementation for redis stream publisher behavior.
 */
@RequiredArgsConstructor
@Getter
public abstract class AbstractRedisStreamPublisher {

    /**
     * Stores redis handler.
     */
    private final AbstractRedisHandler redisHandler;

    /**
     * Constant for add parameters.
     */
    private final XAddParams ADD_PARAMETERS =
            new XAddParams().id(StreamEntryID.NEW_ENTRY).approximateTrimming();

    /**
     * Constant for character set.
     */
    private static final Charset CHARACTER_SET = KStaffConstant.CHARSET;

    /**
     * Executes publish.
     */
    public void publish(EventOuterClass.Event event, String streamKey) {
        this.redisHandler.runCommand(
                jedis -> {
                    Map<byte[], byte[]> map = new HashMap<>();
                    map.put(
                            AbstractRedisStreamConsumer.EVENT_DATA_KEY.getBytes(CHARACTER_SET),
                            event.toByteArray());

                    jedis.xadd(streamKey.getBytes(CHARACTER_SET), ADD_PARAMETERS, map);
                });
    }

    /**
     * Executes publish.
     */
    public void publish(EventOuterClass.EventBundle eventBundle, String streamKey) {
        this.redisHandler.runCommand(
                jedis -> {
                    Map<byte[], byte[]> map = new HashMap<>();
                    map.put(streamKey.getBytes(CHARACTER_SET), eventBundle.toByteArray());

                    jedis.xadd(streamKey.getBytes(CHARACTER_SET), map, ADD_PARAMETERS);
                });
    }
}
