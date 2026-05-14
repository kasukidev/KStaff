package me.kasuki.kstaff.utilities.config;

import cc.insidious.config.Config;
import cc.insidious.config.annotation.ConfigAnnotation;
import com.google.common.collect.Lists;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public class MainConfig extends Config {
    @ConfigAnnotation(path = "server_identifier")
    public static String SERVER_NAME = "DEV";

    @ConfigAnnotation(path = "staff.stafchat.format")
    public static String STAFF_CHAT_FORMAT = "&7[&bSC&7] &7[%server%] &3&l%sender% &8>> &b%message%";


    @ConfigAnnotation(path = "staff.staffmode.enabled_commands")
    public static List<String> ENABLED_STAFFMODE_COMMANDS = Lists.newArrayList(
            "staff",
            "staffmode",
            "sm",
            "mm",
            "modmode"
    );

    @ConfigAnnotation(path = "alerts.center_alert_message")
    public static boolean CENTER_ALERT_MESSAGE = true;

    @ConfigAnnotation(path = "alerts.alert_sound")
    public static boolean ALERT_SOUND = true;


    @ConfigAnnotation(path = "alerts.format")
    public static List<String> ALERT_FORMAT = Lists.newArrayList(
            "",
            "&3&lKStaff Alert",
            "&b%message%",
            ""
    );

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
    public static String REDIS_CONSUMER_KEY = "dev";

    public MainConfig(JavaPlugin plugin) {
        super(plugin, "config");
    }
}
