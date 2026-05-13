package me.kasuki.kstaff.data.redis.consumer;


import java.util.Set;
import java.util.logging.Logger;
import me.kasuki.kstaff.api.database.redis.AbstractRedisHandler;
import me.kasuki.kstaff.api.database.redis.repository.stream.IEventProcessorHandler;
import me.kasuki.kstaff.api.database.redis.repository.stream.consumer.AbstractRedisStreamConsumer;

public class RedisStreamConsumer extends AbstractRedisStreamConsumer {

    public RedisStreamConsumer(AbstractRedisHandler redisHandler, IEventProcessorHandler eventProcessorHandler, Set<String> streamKeys, String consumerGroup, String consumerKey, Logger logger) {
        super(redisHandler, eventProcessorHandler, streamKeys, consumerGroup, consumerKey, logger);
    }
}