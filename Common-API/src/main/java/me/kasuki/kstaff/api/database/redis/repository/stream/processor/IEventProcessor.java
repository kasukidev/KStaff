package me.kasuki.kstaff.api.database.redis.repository.stream.processor;


import ne.kasuki.kstaff.api.database.redis.event.EventOuterClass;

/**
 * Processes event payloads from the stream pipeline.
 */
public interface IEventProcessor {
    /**
     * Gets event type.
     */
    EventOuterClass.EventType getEventType();

    /**
     * Executes process event.
     */
    void processEvent(byte[] data);
}
