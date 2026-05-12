package me.kasuki.kstaff.data.redis.impl;

import com.google.protobuf.InvalidProtocolBufferException;
import lombok.RequiredArgsConstructor;
import me.kasuki.kstaff.KStaffPlugin;
import me.kasuki.kstaff.api.data.redis.Staffmessage;
import me.kasuki.kstaff.api.database.redis.event.EventOuterClass;
import me.kasuki.kstaff.api.database.redis.repository.stream.processor.IEventProcessor;

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
        this.instance.getLogger().info("Received staff chat message from " + staffMessage.getSenderName());
    }
}
