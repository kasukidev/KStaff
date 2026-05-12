package me.kasuki.kstaff.utilities.config;

import cc.insidious.config.Config;
import cc.insidious.config.annotation.ConfigAnnotation;
import com.google.common.collect.Lists;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public class MainConfig extends Config {
    // Redis
    @ConfigAnnotation(path = "redis.host")
    public static String REDIS_HOST = "172.18.0.1";

    @ConfigAnnotation(path = "redis.port")
    public static int REDIS_PORT = 6379;

    @ConfigAnnotation(path = "redis.password")
    public static String REDIS_PASSWORD = "";

    @ConfigAnnotation(path = "redis.channel")
    public static String REDIS_CHANNEL = "kstaff";

    @ConfigAnnotation(path = "redis.consumer_group")
    public static String REDIS_CONSUMER_GROUP = "kstaff-default";

    @ConfigAnnotation(path = "redis.consumer_key")
    public static String REDIS_CONSUMER_KEY = "kstaff-dev";

    @ConfigAnnotation(path = "staffmode.enabled_commands")
    public static List<String> ENABLED_STAFFMODE_COMMANDS = Lists.newArrayList(
            "staff",
            "staffmode",
            "sm",
            "mm",
            "modmode"
    );

    public MainConfig(JavaPlugin plugin) {
        super(plugin, "config");
    }
}
