package me.kasuki.kstaff.data.redis.publisher;

import me.kasuki.kstaff.api.database.redis.AbstractRedisHandler;
import me.kasuki.kstaff.api.database.redis.repository.stream.publisher.AbstractRedisStreamPublisher;

public class RedisStreamPublisher extends AbstractRedisStreamPublisher {
    public RedisStreamPublisher(AbstractRedisHandler redisHandler) {
        super(redisHandler);
    }
}