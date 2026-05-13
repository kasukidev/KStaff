package me.kasuki.kstaff;

import com.google.common.collect.Lists;
import lombok.Getter;
import me.kasuki.kstaff.api.KStaffAPI;
import me.kasuki.kstaff.api.constant.KStaffConstant;
import me.kasuki.kstaff.api.database.redis.repository.stream.IEventProcessorHandler;
import me.kasuki.kstaff.api.database.redis.repository.stream.consumer.AbstractRedisStreamConsumer;
import me.kasuki.kstaff.api.database.redis.repository.stream.publisher.AbstractRedisStreamPublisher;
import me.kasuki.kstaff.api.registration.IRegistrationHandler;
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

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

@Getter
public class KStaffPlugin extends JavaPlugin {
    private KStaffAPI KStaffAPI;
    private SaveRunnable saveRunnable;
    private ItemManager itemManager;
    private StaffManager staffManager;

    // Redis stuff
    private RedisHandler redisHandler;
    private AbstractRedisStreamPublisher redisStreamPublisher;
    private AbstractRedisStreamConsumer redisStreamConsumer;

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

        this.registerItemManager();
        this.staffManager = new StaffManager(this);
        this.saveRunnable = new SaveRunnable(this);
    }

    @Override
    public void onDisable() {
        if (this.saveRunnable != null) {
            this.saveRunnable.cancel();
            this.saveRunnable = null;
        }

        this.KStaffAPI.shutdown();
    }

    private void initRedis() {
        this.redisHandler = new RedisHandler(
                MainConfig.REDIS_HOST, MainConfig.REDIS_PORT,
                MainConfig.REDIS_PASSWORD, MainConfig.REDIS_CHANNEL
        );

        this.redisStreamPublisher = new RedisStreamPublisher(redisHandler);

        Set<String> keys = new HashSet<>(Lists.newArrayList(KStaffConstant.STAFF_CHAT_REDIS_KEY));
        IEventProcessorHandler eventProcessorHandler = new EventProcessorHandler(this);
        eventProcessorHandler.load();

        this.redisStreamConsumer = new RedisStreamConsumer(this.redisHandler, eventProcessorHandler, keys, MainConfig.REDIS_CONSUMER_GROUP, MainConfig.REDIS_CONSUMER_KEY, this.getLogger());
        this.redisStreamConsumer.startConsumption();
    }


    private void registerItemManager(){
        this.itemManager = new ItemManager(this);
        this.itemManager.init();
    }
}
