package me.kasuki.kstaff.data.redis;

import me.kasuki.kstaff.api.database.redis.AbstractRedisHandler;

public class RedisHandler extends AbstractRedisHandler {

    public RedisHandler(String host, int port, String password, String channel) {
        super(host, port, password, channel);
    }
}