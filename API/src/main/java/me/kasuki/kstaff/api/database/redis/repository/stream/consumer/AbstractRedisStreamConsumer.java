package me.kasuki.kstaff.api.database.redis.repository.stream.consumer;

import com.google.protobuf.InvalidProtocolBufferException;
import lombok.RequiredArgsConstructor;
import me.kasuki.kstaff.api.constant.KStaffConstant;
import me.kasuki.kstaff.api.database.redis.AbstractRedisHandler;
import me.kasuki.kstaff.api.database.redis.event.EventOuterClass;
import me.kasuki.kstaff.api.database.redis.repository.stream.IEventProcessorHandler;
import me.kasuki.kstaff.api.database.redis.repository.stream.processor.IEventProcessor;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.StreamEntryID;
import redis.clients.jedis.exceptions.JedisDataException;
import redis.clients.jedis.params.XPendingParams;
import redis.clients.jedis.params.XReadGroupParams;

import java.nio.charset.Charset;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Base implementation for redis stream consumer behavior.
 */
@RequiredArgsConstructor
public abstract class AbstractRedisStreamConsumer {

    /**
     * Stores redis handler.
     */
    private final AbstractRedisHandler redisHandler;

    /**
     * Stores event processor handler.
     */
    private final IEventProcessorHandler eventProcessorHandler;

    /**
     * Stores stream keys.
     */
    private final Set<String> streamKeys;

    /**
     * Stores consumer group.
     */
    private final String consumerGroup;

    /**
     * Stores consumer key.
     */
    private final String consumerKey;

    /**
     * Stores logger.
     */
    private final Logger logger;

    /**
     * Constant for event data key.
     */
    public static final String EVENT_DATA_KEY = "event_data";

    /**
     * Constant for read group params.
     */
    public static final XReadGroupParams READ_GROUP_PARAMS =
            new XReadGroupParams().block(1000).count(100_000);

    /**
     * Constant for pending group params.
     */
    public static final XPendingParams PENDING_GROUP_PARAMS =
            new XPendingParams()
                    .start(new StreamEntryID("0-0"))
                    .end(StreamEntryID.MAXIMUM_ID)
                    .count(100_000);

    /**
     * Stores executor.
     */
    private ExecutorService executor = Executors.newSingleThreadExecutor();

    /**
     * Executes add stream key.
     */
    public void addStreamKey(String streamKey) {
        this.streamKeys.add(streamKey);
    }

    /**
     * Executes remove stream key.
     */
    public void removeStreamKey(String streamKey) {
        this.streamKeys.remove(streamKey);
    }

    /**
     * Executes contains stream key.
     */
    public boolean containsStreamKey(String streamKey) {
        return this.streamKeys.contains(streamKey);
    }

    /**
     * Executes start consumption.
     */
    public void startConsumption() {
        if (this.executor == null || this.executor.isShutdown()) {
            this.executor = Executors.newSingleThreadExecutor();
        }
        this.executor.execute(
                () -> {
                    String password = this.redisHandler.getPassword();
                    try (Jedis jedis = this.redisHandler.getSubscriptionPool().getResource()) {
                        if (jedis == null) {
                            return;
                        }
                        if (!password.isEmpty()) {
                            jedis.auth(password);
                        }

                        this.ensureConsumerGroupExists(jedis);

                        Charset charset = KStaffConstant.CHARSET;

                        StreamEntryID streamEntryID = StreamEntryID.UNRECEIVED_ENTRY;

                        while (!Thread.currentThread().isInterrupted()) {
                            this.checkPendingMessages(jedis);

                            Map<byte[], StreamEntryID> streams = new ConcurrentHashMap<>();
                            streamKeys.forEach(key -> streams.put(key.getBytes(charset), streamEntryID));

                            this.processStreamKeys(streams, charset, jedis);
                        }
                    }
                });
    }

    /**
     * Executes ensure consumer group exists.
     */
    private void ensureConsumerGroupExists(Jedis jedis) {
        for (String streamKey : streamKeys) {
            try {
                // Try to create consumer group if it doesn't exist
                jedis.xgroupCreate(
                        streamKey.getBytes(KStaffConstant.CHARSET),
                        consumerGroup.getBytes(KStaffConstant.CHARSET),
                        "0-0".getBytes(KStaffConstant.CHARSET),
                        true);
            } catch (JedisDataException exception) {
                if (!exception.getMessage().contains("BUSYGROUP")) {
                    // Ignore BUSYGROUP (group already exists)
                    logger.log(Level.WARNING, "Failed to create consumer group", exception);
                }
            }
        }
    }

    /**
     * Executes process stream keys.
     */
    private void processStreamKeys(Map<byte[], StreamEntryID> streams, Charset charset, Jedis jedis) {
        for (Map.Entry<byte[], StreamEntryID> entry : streams.entrySet()) {
            Map.Entry<byte[], byte[]> redisStreamEntry =
                    new AbstractMap.SimpleEntry<>(
                            entry.getKey(), entry.getValue().toString().getBytes(KStaffConstant.CHARSET));

            List<Object> allMessages =
                    jedis.xreadGroup(
                            this.consumerGroup.getBytes(charset),
                            this.consumerKey.getBytes(charset),
                            READ_GROUP_PARAMS,
                            redisStreamEntry);

            if (allMessages == null) {
                continue;
            }

            this.processStreamMessages(allMessages, jedis);
        }
    }

    /**
     * Executes check pending messages.
     */
    private void checkPendingMessages(Jedis jedis) {
        for (String streamKey : streamKeys) {
            List<Object> pending =
                    jedis.xpending(
                            streamKey.getBytes(KStaffConstant.CHARSET),
                            consumerGroup.getBytes(KStaffConstant.CHARSET),
                            PENDING_GROUP_PARAMS);

            if (!pending.isEmpty()) {
                processStreamMessages(pending, jedis);
            }
        }
    }

    /**
     * Executes process stream messages.
     */
    private void processStreamMessages(List<Object> messages, Jedis jedis) {
        for (Object streamObj : messages) {
            if (!(streamObj instanceof List<?>)) {
                continue;
            }

            List<Object> streamList = (List<Object>) streamObj;

            if (streamList.size() < 2) {
                continue;
            }

            Object streamKeyObject = streamList.get(0);
            Object listObject = streamList.get(1);

            if (!(streamKeyObject instanceof byte[]) || !(listObject instanceof List<?>)) {
                continue;
            }

            byte[] streamKeyRaw = (byte[]) streamKeyObject;

            List<Object> entries = (List<Object>) listObject;

            this.processEntriesList(entries, streamKeyRaw, jedis);
        }
    }

    /**
     * Executes process entries list.
     */
    private void processEntriesList(List<Object> entries, byte[] streamKey, Jedis jedis) {
        for (Object entryObj : entries) {
            if (!(entryObj instanceof List<?>)) {
                continue;
            }

            List<Object> entryList = (List<Object>) entryObj;
            byte[] entryIdRaw = (byte[]) entryList.get(0);
            String entryId = new String(entryIdRaw, KStaffConstant.CHARSET);

            Object entryListObject = entryList.get(1);

            if (!(entryListObject instanceof List<?>)) {
                continue;
            }

            List<Object> fieldList = (List<Object>) entryListObject;
            this.processFieldList(fieldList, entryId, streamKey, jedis);
        }
    }

    /**
     * Executes process field list.
     */
    private void processFieldList(
            List<Object> fieldList, String entryId, byte[] streamKey, Jedis jedis) {
        int maxIndex = fieldList.size() - 1;
        for (int index = 0; index < fieldList.size(); index++) {
            String fieldName = new String((byte[]) fieldList.get(index), KStaffConstant.CHARSET);

            if (!fieldName.equals(EVENT_DATA_KEY)) {
                continue;
            }

            int dataIndex = index + 1;

            if (dataIndex > maxIndex) {
                continue;
            }
            byte[] data = (byte[]) fieldList.get(dataIndex);
            this.processEvent(data, entryId, streamKey, jedis);
        }
    }

    /**
     * Executes process event.
     */
    private void processEvent(byte[] input, String entryId, byte[] streamKey, Jedis jedis) {
        if (this.eventProcessorHandler == null) {
            this.logger.severe("The EventProcessorHandler is null, streams cannot be processed!");
            return;
        }

        try {
            if (this.attemptEventConstruction(input, streamKey, entryId, jedis)) {
                return;
            }

            if (this.attemptEventBundleConstruction(input, streamKey, entryId, jedis)) {
                return;
            }

            String logMessage = String.format("Failed to find a suitable message type for %s", entryId);
            this.logger.warning(logMessage);
        } catch (Exception exception) {
            String logMessage = String.format("Failed to process event %s", entryId);
            this.logger.log(Level.SEVERE, logMessage, exception);
        }
    }

    /**
     * Executes attempt event construction.
     */
    private boolean attemptEventConstruction(
            byte[] input, byte[] streamKey, String entryId, Jedis jedis) {
        try {
            EventOuterClass.Event event = EventOuterClass.Event.parseFrom(input);
            this.processReceivedEvent(event);
            jedis.xack(
                    new String(streamKey, KStaffConstant.CHARSET),
                    consumerGroup,
                    new StreamEntryID(entryId));
            return true;
        } catch (InvalidProtocolBufferException ignored) {

        }
        return false;
    }

    /**
     * Executes attempt event bundle construction.
     */
    private boolean attemptEventBundleConstruction(
            byte[] input, byte[] streamKey, String entryId, Jedis jedis) {
        try {
            EventOuterClass.EventBundle eventBundle = EventOuterClass.EventBundle.parseFrom(input);
            this.processReceivedEventBundle(eventBundle);
            jedis.xack(
                    new String(streamKey, KStaffConstant.CHARSET),
                    consumerGroup,
                    new StreamEntryID(entryId));
            return true;
        } catch (InvalidProtocolBufferException ignored) {

        }
        return false;
    }

    /**
     * Executes process received event.
     */
    private void processReceivedEvent(EventOuterClass.Event event) {
        Optional<IEventProcessor> optional =
                this.eventProcessorHandler.getEventProcessor(event.getEventType());

        if (!optional.isPresent()) {
            this.logger.warning(
                    String.format("No event processor found for %s", event.getEventType().name()));
            return;
        }

        this.logger.info("Processing Event for: " + event.getEventType().name());
        IEventProcessor processor = optional.get();
        processor.processEvent(event.getEventData().toByteArray());
    }

    /**
     * Executes process received event bundle.
     */
    private void processReceivedEventBundle(EventOuterClass.EventBundle eventBundle) {
        List<EventOuterClass.Event> events = eventBundle.getEventsList();

        int eventCount = events.size();
        int batchSize = 1000;

        if (eventCount <= 1000) {
            this.processSubList(events);
            return;
        }

        for (int index = 0; index < eventCount; index += batchSize) {
            int endIndex = Math.min(index + batchSize, eventCount);

            List<EventOuterClass.Event> subList = events.subList(index, endIndex);
            this.processSubList(subList);
        }
    }

    /**
     * Executes process sub list.
     */
    private void processSubList(List<EventOuterClass.Event> events) {
        for (EventOuterClass.Event event : events) {
            this.processReceivedEvent(event);
        }
    }

    /**
     * Executes stop consumption.
     */
    public void stopConsumption() {
        executor.shutdown();

        try {
            if (!executor.awaitTermination(5000L, TimeUnit.MILLISECONDS)) {
                executor.shutdownNow();
                logger.warning("Forced shutdown after timeout");
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
            logger.severe("Shutdown interrupted");
        }
    }
}
