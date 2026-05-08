package me.kasuki.kstaff.config;

import cc.insidious.config.Config;
import org.bukkit.plugin.java.JavaPlugin;

public class LangConfig extends Config {
    public LangConfig(JavaPlugin plugin) {
        super(plugin, "lang");
    }
}
