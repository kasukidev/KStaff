package me.kasuki.kstaff;

import com.google.common.collect.Lists;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;
import lombok.Getter;
import me.kasuki.kstaff.api.KStaffAPI;
import me.kasuki.kstaff.api.constant.KStaffConstant;
import me.kasuki.kstaff.api.database.redis.repository.stream.IEventProcessorHandler;
import me.kasuki.kstaff.api.database.redis.repository.stream.consumer.AbstractRedisStreamConsumer;
import me.kasuki.kstaff.api.database.redis.repository.stream.publisher.AbstractRedisStreamPublisher;
import me.kasuki.kstaff.api.registration.IRegistrationHandler;
import me.kasuki.kstaff.chat.ChatManager;
import me.kasuki.kstaff.data.redis.RedisHandler;
import me.kasuki.kstaff.data.redis.consumer.RedisStreamConsumer;
import me.kasuki.kstaff.data.redis.consumer.processor.EventProcessorHandler;
import me.kasuki.kstaff.data.redis.publisher.RedisStreamPublisher;
import me.kasuki.kstaff.item.ItemManager;
import me.kasuki.kstaff.registration.data.ConfigRegistrationHandler;
import me.kasuki.kstaff.registration.data.ModuleRegistrationHandler;
import me.kasuki.kstaff.registration.gameplay.CommandRegistrationHandler;
import me.kasuki.kstaff.registration.gameplay.ListenerRegistrationHandler;
import me.kasuki.kstaff.save.SaveRunnable;
import me.kasuki.kstaff.staff.StaffManager;
import me.kasuki.kstaff.utilities.config.MainConfig;
import org.bukkit.plugin.java.JavaPlugin;

@Getter
public class KStaffPlugin extends JavaPlugin {
    // API
    private KStaffAPI KStaffAPI;
    private SaveRunnable saveRunnable;

    // Custom items
    private ItemManager itemManager;

    // Redis
    private RedisHandler redisHandler;
    private AbstractRedisStreamPublisher redisStreamPublisher;
    private AbstractRedisStreamConsumer redisStreamConsumer;

    // Staff
    private final Set<UUID> staffPlayers = new HashSet<>();
    private StaffManager staffManager;

    // Chat
    private ChatManager chatManager;

    /**
     * Core onEnable
     */
    @Override
    public void onEnable() {
        new ConfigRegistrationHandler(this).registerObjects();
        this.KStaffAPI = new KStaffAPI(this.getLogger());
        this.initRedis();

        Stream.of(
                        new ModuleRegistrationHandler(this, this.KStaffAPI),
                        new CommandRegistrationHandler(this),
                        new ListenerRegistrationHandler(this))
                .forEachOrdered(IRegistrationHandler::registerObjects);

        this.initItemManager();
        this.initManagers();
        this.saveRunnable = new SaveRunnable(this);
    }

    /**
     * Core onDisable
     */
    @Override
    public void onDisable() {
        if (this.saveRunnable != null) {
            this.saveRunnable.cancel();
            this.saveRunnable = null;
        }

        this.KStaffAPI.shutdown();
    }

    /**
     * Initialization
     */
    private void initRedis() {
        this.redisHandler = new RedisHandler(
                MainConfig.REDIS_HOST, MainConfig.REDIS_PORT,
                MainConfig.REDIS_PASSWORD, MainConfig.REDIS_CHANNEL
        );

        this.redisStreamPublisher = new RedisStreamPublisher(redisHandler);

        Set<String> keys = new HashSet<>(Lists.newArrayList(KStaffConstant.STAFF_CHAT_REDIS_KEY, KStaffConstant.ALERT_REDIS_KEY));
        IEventProcessorHandler eventProcessorHandler = new EventProcessorHandler(this);
        eventProcessorHandler.load();

        this.redisStreamConsumer = new RedisStreamConsumer(this.redisHandler, eventProcessorHandler, keys, MainConfig.REDIS_CONSUMER_GROUP, MainConfig.REDIS_CONSUMER_KEY, this.getLogger());
        this.redisStreamConsumer.startConsumption();
    }

    private void initManagers(){
        this.chatManager = new ChatManager(this);
        this.staffManager = new StaffManager(this);
    }

    private void initItemManager(){
        this.itemManager = new ItemManager(this);
        this.itemManager.init();
    }
}
