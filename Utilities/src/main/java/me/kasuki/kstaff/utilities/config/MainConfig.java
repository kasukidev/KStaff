package me.kasuki.kstaff.utilities.config;

import cc.insidious.config.Config;
import org.bukkit.plugin.java.JavaPlugin;

public class MainConfig extends Config {
    public MainConfig(JavaPlugin plugin) {
        super(plugin, "config");
    }
}
