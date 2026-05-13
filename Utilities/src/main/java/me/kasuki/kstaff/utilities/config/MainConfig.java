package me.kasuki.kstaff.utilities.config;

import cc.insidious.config.Config;
import cc.insidious.config.annotation.ConfigAnnotation;
import com.google.common.collect.Lists;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public class MainConfig extends Config {
    @ConfigAnnotation(path = "server_identifier")
    public static String SERVER_NAME = "DEV";

    @ConfigAnnotation(path = "staffchat.format")
    public static String STAFF_CHAT_FORMAT = "&e[&b%server%&e] &7[&e%sender%&7] &f%message%";



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
    public static String REDIS_CONSUMER_GROUP = "kstaff";

    @ConfigAnnotation(path = "redis.consumer_key")
    public static String REDIS_CONSUMER_KEY = "kstaff-dev";


    // Server
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
