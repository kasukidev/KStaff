package me.kasuki.kstaff.data.redis.consumer.processor;

import me.kasuki.kstaff.KStaffPlugin;
import me.kasuki.kstaff.api.database.redis.event.EventOuterClass;
import me.kasuki.kstaff.api.database.redis.repository.stream.IEventProcessorHandler;
import me.kasuki.kstaff.api.database.redis.repository.stream.processor.IEventProcessor;
import me.kasuki.kstaff.data.redis.impl.StaffChatEventProcessor;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

public class EventProcessorHandler implements IEventProcessorHandler {
    private final KStaffPlugin instance;

    private final Map<EventOuterClass.EventType, IEventProcessor> processors = new ConcurrentHashMap<>();

    public EventProcessorHandler(KStaffPlugin instance) {
        this.instance = instance;
    }

    @Override
    public void registerEventProcessor(IEventProcessor processor) {
        this.processors.put(processor.getEventType(), processor);
    }

    @Override
    public void unregisterEventProcessor(IEventProcessor processor) {
        this.processors.remove(processor.getEventType());
    }

    @Override
    public boolean hasEventProcessor(EventOuterClass.EventType eventType) {
        return this.processors.containsKey(eventType);
    }

    @Override
    public Optional<IEventProcessor> getEventProcessor(EventOuterClass.EventType type) {
        return Optional.ofNullable(this.processors.get(type));
    }

    @Override
    public void load() {
        Stream.of(new StaffChatEventProcessor(this.instance))
                .filter(IEventProcessor.class::isInstance)
                .forEach(this::registerEventProcessor);
    }
}