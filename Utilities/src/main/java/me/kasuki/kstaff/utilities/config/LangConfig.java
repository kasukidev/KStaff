package me.kasuki.kstaff.utilities.config;

import cc.insidious.config.Config;
import cc.insidious.config.annotation.ConfigAnnotation;
import org.bukkit.plugin.java.JavaPlugin;

public class LangConfig extends Config {
    @ConfigAnnotation(path = "prefix")
    public static String PREFIX = "&8[&3&lKStaff&8]";

    /**
     * General errors
     */
    @ConfigAnnotation(path = "errors.profile_not_found")
    public static String PROFILE_NOT_FOUND = "&cYour profile was not found! Please try rejoining or contact staff if this issue persists.";

    @ConfigAnnotation(path = "errors.cooldown_active")
    public static String COOLDOWN_ACTIVE = "&cYou are on cooldown for %time%ms!";

    @ConfigAnnotation(path = "errors.no_breaking_in_sm")
    public static String NO_BREAKING_BLOCKS = "&cYou may not break blocks in staffmode!";


    @ConfigAnnotation(path = "errors.no_placing_in_sm")
    public static String NO_PLACING_BLOCKS = "&cYou may not place blocks in staffmode!";

    @ConfigAnnotation(path = "errors.no_dropping_in_sm")
    public static String NO_DROPPING_ITEMS = "&cYou may not drop items in staffmode!";

    @ConfigAnnotation(path = "errors.command_disabled")
    public static String COMMAND_DISABLED = "&cYou may not use this command while in staffmode!";

    /**
     * Other staffmode conf
     */
    @ConfigAnnotation(path = "staffmode.staff_enabled")
    public static String STAFFMODE_ENABLED = "&aYou have &nenabled&a staffmode!";

    @ConfigAnnotation(path = "staffmode.staff_disabled")
    public static String STAFFMODE_DISABLED = "&cYou have &ndisabled&c staffmode!";

    @ConfigAnnotation(path = "staffchat.staff_enabled")
    public static String STAFFCHAT_ENABLED = "&aYou have &nenabled&a staff chat!";

    @ConfigAnnotation(path = "staffchat.staff_disabled")
    public static String STAFFCHAT_DISABLED = "&cYou have &ndisabled&c staff chat!";

    public LangConfig(JavaPlugin plugin) {
        super(plugin, "lang");
    }
}
