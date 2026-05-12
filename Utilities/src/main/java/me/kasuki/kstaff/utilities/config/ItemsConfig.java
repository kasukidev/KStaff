package me.kasuki.kstaff.utilities.config;

import cc.insidious.config.Config;
import cc.insidious.config.annotation.ConfigAnnotation;
import com.google.common.collect.Lists;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public class ItemsConfig extends Config {
    @ConfigAnnotation(path = "random_teleport.item_name")
    public static String RANDOM_TELEPORT_ITEM_NAME = "&a&lRandom Teleport";

    @ConfigAnnotation(path = "random_teleport.item_lore")
    public static List<String> RANDOM_TELEPORT_ITEM_LORE = Lists.newArrayList(
            "&7Click to teleport to a random",
            "&7online player that is not you!"
    );

    @ConfigAnnotation(path = "random_teleport.slot", comment = "Slot number starting from 0")
    public static int RANDOM_TELEPORT_SLOT = 4;

    @ConfigAnnotation(path = "random_teleport.material")
    public static String RANDOM_TELEPORT_MATERIAL = "REDSTONE";

    @ConfigAnnotation(path = "random_teleport.no_online_players")
    public static String RANDOM_TELEPORT_NO_ONLINE_PLAYERS = "&cThere are no online players to teleport to!";

    @ConfigAnnotation(path = "random_teleport.teleport_success")
    public static String RANDOM_TELEPORT_SUCCESS = "&aYou have been teleported to %player%!";

    public ItemsConfig(JavaPlugin plugin) {
        super(plugin, "items");
    }
}
