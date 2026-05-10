package me.kasuki.kstaff.utilities.config;

import cc.insidious.config.Config;
import cc.insidious.config.annotation.ConfigAnnotation;
import org.bukkit.plugin.java.JavaPlugin;

public class LangConfig extends Config {
    @ConfigAnnotation(path = "prefix")
    public static String PREFIX = "&8[&3&lKStaff&8]";

    @ConfigAnnotation(path = "errors.profile_not_found")
    public static String PROFILE_NOT_FOUND = "&cYour profile was not found! Please try rejoining or contact staff if this issue persists.";

    public LangConfig(JavaPlugin plugin) {
        super(plugin, "lang");
    }
}
