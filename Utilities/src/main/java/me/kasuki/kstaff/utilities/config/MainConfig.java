package me.kasuki.kstaff.utilities.config;

import cc.insidious.config.Config;
import cc.insidious.config.annotation.ConfigAnnotation;
import com.google.common.collect.Lists;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public class MainConfig extends Config {
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
