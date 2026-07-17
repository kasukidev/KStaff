package me.kasuki.kstaff.chat;

import com.cryptomorin.xseries.XSound;
import me.kasuki.kstaff.KStaffPlugin;
import me.kasuki.kstaff.api.constant.KStaffConstant;
import me.kasuki.kstaff.api.data.redis.AlertOuterClass;
import me.kasuki.kstaff.api.database.redis.event.EventOuterClass;
import me.kasuki.kstaff.api.database.redis.repository.stream.publisher.AbstractRedisStreamPublisher;
import me.kasuki.kstaff.utilities.chat.CC;
import me.kasuki.kstaff.utilities.chat.MessageUtil;
import me.kasuki.kstaff.utilities.config.MainConfig;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class ChatManager {
    private final KStaffPlugin instance;

    public ChatManager(KStaffPlugin instance) {
        this.instance = instance;
    }

    /**
     * Alert handling
     */
    public void publishAlert(String message){
        AlertOuterClass.Alert alert = AlertOuterClass.Alert.newBuilder()
                .setMessage(message)
                .setServerFrom(MainConfig.SERVER_NAME)
                .build();

        EventOuterClass.Event protoEvent = EventOuterClass.Event.newBuilder()
                .setEventType(EventOuterClass.EventType.ALERT)
                .setEventData(alert.toByteString())
                .build();

        AbstractRedisStreamPublisher publisher = this.instance.getRedisStreamPublisher();
        publisher.publish(protoEvent, KStaffConstant.ALERT_REDIS_KEY);
    }

    public void handleAlertBroadcast(String message){
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (MainConfig.ALERT_SOUND) {
                player.playSound(player.getLocation(), XSound.BLOCK_NOTE_BLOCK_PLING.get(), 1.0F, 1.2F);
            }

            MainConfig.ALERT_FORMAT.forEach(str -> {
                String alertStr = CC.chat(str.replace("%message%", message));

                if (MainConfig.CENTER_ALERT_MESSAGE) {
                    MessageUtil.sendCenteredMessage(player, alertStr);
                    return;
                }

                player.sendMessage(alertStr);
            });
        }
    }
}
