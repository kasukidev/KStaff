package me.kasuki.kstaff.chat.command;

import cc.insidious.fethmusmioma.annotation.Command;
import cc.insidious.fethmusmioma.annotation.Parameter;
import me.kasuki.kstaff.KStaffPlugin;
import me.kasuki.kstaff.chat.ChatManager;
import org.bukkit.entity.Player;

/**
 * Alert message to all connected redis instances
 */
public class CommandAlert {
    private final KStaffPlugin instance;

    public CommandAlert(KStaffPlugin instance) {
        this.instance = instance;
    }

    @Command(label = "alert", aliases = {"globalalert", "galert"}, permission = "kstaff.alert", appendStrings = true)
    public void executeAlert(Player player, @Parameter(name = "message") String message){
        ChatManager chatManager = this.instance.getChatManager();
        chatManager.publishAlert(message);
    }
}
