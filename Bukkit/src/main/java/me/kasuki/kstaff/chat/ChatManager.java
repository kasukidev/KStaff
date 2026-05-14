package me.kasuki.kstaff.chat;

import lombok.RequiredArgsConstructor;
import me.kasuki.kstaff.KStaffPlugin;
import me.kasuki.kstaff.api.constant.KStaffConstant;
import me.kasuki.kstaff.api.data.redis.AlertOuterClass;
import me.kasuki.kstaff.api.database.redis.event.EventOuterClass;
import me.kasuki.kstaff.api.database.redis.repository.stream.publisher.AbstractRedisStreamPublisher;
import me.kasuki.kstaff.utilities.config.MainConfig;

@RequiredArgsConstructor
public class ChatManager {
    private final KStaffPlugin instance;

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

}
