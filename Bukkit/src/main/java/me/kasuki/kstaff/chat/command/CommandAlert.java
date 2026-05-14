package me.kasuki.kstaff.chat.command;

import cc.insidious.fethmusmioma.annotation.Command;
import cc.insidious.fethmusmioma.annotation.Parameter;
import me.kasuki.kstaff.KStaffPlugin;
import me.kasuki.kstaff.chat.ChatManager;
import me.kasuki.kstaff.utilities.chat.MessageUtil;
import me.kasuki.kstaff.utilities.config.LangConfig;
import me.kasuki.kstaff.utilities.cooldown.Cooldown;
import org.bukkit.entity.Player;

/**
 * Alert message to all connected redis instances
 */
public class CommandAlert {
    private final KStaffPlugin instance;
    private final Cooldown cooldown;

    public CommandAlert(KStaffPlugin instance) {
        this.instance = instance;
        this.cooldown = new Cooldown();
    }

    @Command(label = "alert", aliases = {"globalalert", "galert"}, permission = "kstaff.alert", appendStrings = true)
    public void executeAlert(Player player, @Parameter(name = "message") String message){
        if(this.cooldown.isActive(player.getUniqueId())){
            MessageUtil.sendPrefixedMessage(player, LangConfig.COOLDOWN_ACTIVE.replace("%time%", cooldown.getRemaining(player.getUniqueId()) + ""));
            return;
        }

        ChatManager chatManager = this.instance.getChatManager();
        chatManager.publishAlert(message);
        chatManager.handleAlertBroadcast(message);
        cooldown.placeOnCooldown(player.getUniqueId(), 100L);
    }
}
