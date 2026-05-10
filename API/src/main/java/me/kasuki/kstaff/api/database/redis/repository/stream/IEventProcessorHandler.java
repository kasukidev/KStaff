package me.kasuki.kstaff.api.database.redis.repository.stream;

import me.kasuki.kstaff.api.database.redis.event.EventOuterClass;
import me.kasuki.kstaff.api.database.redis.repository.stream.processor.IEventProcessor;

import java.util.Optional;

/**
 * Coordinates registration and execution of event processors.
 */
public interface IEventProcessorHandler {

    /**
     * Executes register event processor.
     */
    void registerEventProcessor(IEventProcessor processor);

    /**
     * Executes unregister event processor.
     */
    void unregisterEventProcessor(IEventProcessor processor);

    /**
     * Checks whether event processor.
     */
    boolean hasEventProcessor(EventOuterClass.EventType eventType);

    /**
     * Gets event processor.
     */
    Optional<IEventProcessor> getEventProcessor(EventOuterClass.EventType type);

    /**
     * Executes load.
     */
    void load();
}
