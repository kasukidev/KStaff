package me.kasuki.kstaff.data.redis.impl;

import com.google.protobuf.InvalidProtocolBufferException;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import me.kasuki.kstaff.KStaffPlugin;
import me.kasuki.kstaff.api.data.redis.Staffmessage;
import me.kasuki.kstaff.api.database.redis.event.EventOuterClass;
import me.kasuki.kstaff.api.database.redis.repository.stream.processor.IEventProcessor;
import me.kasuki.kstaff.utilities.chat.CC;
import me.kasuki.kstaff.utilities.config.MainConfig;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

@RequiredArgsConstructor
public class StaffChatEventProcessor implements IEventProcessor {
    private final KStaffPlugin instance;

    @Override
    public EventOuterClass.EventType getEventType() {
        return EventOuterClass.EventType.STAFF_CHAT_MESSAGE;
    }

    @Override
    public void processEvent(byte[] data) {
        try {
            Staffmessage.StaffMessage staffMessage = Staffmessage.StaffMessage.parseFrom(data);
            this.processRequest(staffMessage);
        } catch (InvalidProtocolBufferException exception) {
            exception.printStackTrace();
        }
    }

    private void processRequest(Staffmessage.StaffMessage staffMessage) {
        Set<UUID> staffPlayers = this.instance.getStaffPlayers();
        staffPlayers.forEach(uuid -> {
            Player player = Bukkit.getPlayer(uuid);
            if (player == null || !player.isOnline() || !player.hasPermission("kstaff.staffchat")){
                staffPlayers.remove(uuid);
                return;
            }

            String formattedMessage = MainConfig.STAFF_CHAT_FORMAT
                    .replace("%sender%", staffMessage.getSenderName())
                    .replace("%server%", staffMessage.getServerFrom())
                    .replace("%message%", staffMessage.getMessage());

            player.sendMessage(CC.chat(formattedMessage));
        });
    }
}
