package me.kasuki.kstaff.utilities.chat;

import lombok.experimental.UtilityClass;
import me.kasuki.kstaff.utilities.config.LangConfig;
import org.bukkit.entity.Player;

@UtilityClass
public class MessageUtil {
    public void sendPrefixedMessage(Player player, String message){
        player.sendMessage(CC.chat(LangConfig.PREFIX + " " + message));
    }
}
