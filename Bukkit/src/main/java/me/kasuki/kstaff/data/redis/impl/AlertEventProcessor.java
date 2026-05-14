package me.kasuki.kstaff.data.redis.impl;

import com.google.protobuf.InvalidProtocolBufferException;
import lombok.RequiredArgsConstructor;
import me.kasuki.kstaff.KStaffPlugin;
import me.kasuki.kstaff.api.data.redis.AlertOuterClass;
import me.kasuki.kstaff.api.database.redis.event.EventOuterClass;
import me.kasuki.kstaff.api.database.redis.repository.stream.processor.IEventProcessor;
import me.kasuki.kstaff.chat.ChatManager;
import me.kasuki.kstaff.utilities.config.MainConfig;

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
        if(alert.getServerFrom().equalsIgnoreCase(MainConfig.SERVER_NAME)) return;
        ChatManager chatManager = this.instance.getChatManager();
        chatManager.publishAlert(alert.getMessage());
    }
}
