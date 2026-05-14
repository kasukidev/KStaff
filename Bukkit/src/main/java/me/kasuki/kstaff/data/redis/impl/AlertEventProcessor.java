package me.kasuki.kstaff.data.redis.impl;

import com.cryptomorin.xseries.XSound;
import com.google.protobuf.InvalidProtocolBufferException;
import lombok.RequiredArgsConstructor;
import me.kasuki.kstaff.KStaffPlugin;
import me.kasuki.kstaff.api.data.redis.AlertOuterClass;
import me.kasuki.kstaff.api.database.redis.event.EventOuterClass;
import me.kasuki.kstaff.api.database.redis.repository.stream.processor.IEventProcessor;
import me.kasuki.kstaff.utilities.chat.CC;
import me.kasuki.kstaff.utilities.chat.MessageUtil;
import me.kasuki.kstaff.utilities.config.MainConfig;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

@RequiredArgsConstructor
public class AlertEventProcessor implements IEventProcessor {
    private final KStaffPlugin instance;

    @Override
    public EventOuterClass.EventType getEventType() {
        return EventOuterClass.EventType.ALERT;
    }

    @Override
    public void processEvent(byte[] data) {
        try {
            AlertOuterClass.Alert alert = AlertOuterClass.Alert.parseFrom(data);
            this.processRequest(alert);
        } catch (InvalidProtocolBufferException exception) {
            exception.printStackTrace();
        }
    }

    private void processRequest(AlertOuterClass.Alert alert) {
        final boolean center = MainConfig.CENTER_ALERT_MESSAGE;
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (MainConfig.ALERT_SOUND) {
                player.playSound(player.getLocation(), XSound.BLOCK_NOTE_BLOCK_PLING.get(), 1.0F, 1.2F);
            }



            MainConfig.ALERT_FORMAT.forEach(str -> {
                String message = CC.chat(str.replace("%message%", alert.getMessage()));

                if (center) {
                    MessageUtil.sendCenteredMessage(player, message);
                    return;
                }

                player.sendMessage(message);
            });
        }
    }
}
