package me.kasuki.kstaff.utilities.chat;

import lombok.experimental.UtilityClass;
import me.kasuki.kstaff.utilities.chat.centered.DefaultFontInfo;
import me.kasuki.kstaff.utilities.config.LangConfig;
import org.bukkit.entity.Player;

@UtilityClass
public class MessageUtil {
    private final int CENTER_PX = 154;

    public void sendPrefixedMessage(Player player, String message){
        player.sendMessage(CC.chat(LangConfig.PREFIX + " " + message));
    }

    // NOTE: Similar to the DefaultFontInfo, found this method on spigot because once again, I couldn't be asked to make it.
    // If it leads to performance issues I'll end up recoding.
    public void sendCenteredMessage(Player player, String message) {
        if (message == null || message.isEmpty()) {
            player.sendMessage("");
            return;
        }

        message = CC.chat(message);

        int messageWidth = 0;
        boolean bold = false;

        for (int i = 0; i < message.length(); i++) {
            char c = message.charAt(i);

            if (c == '§' && i + 1 < message.length()) {
                char format = message.charAt(++i);
                bold = format == 'l' || format == 'L';
                continue;
            }

            DefaultFontInfo font = DefaultFontInfo.getDefaultFontInfo(c);
            messageWidth += (bold ? font.getBoldLength() : font.getLength()) + 1;
        }

        int paddingPx = CENTER_PX - (messageWidth / 2);
        int spaceWidth = DefaultFontInfo.SPACE.getLength() + 1;

        int spaces = Math.max(0, paddingPx / spaceWidth);

        player.sendMessage(new String(new char[spaces]).replace('\0', ' ') + message);
    }
}
