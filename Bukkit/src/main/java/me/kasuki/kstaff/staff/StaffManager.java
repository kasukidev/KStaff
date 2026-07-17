package me.kasuki.kstaff.staff;

import com.cryptomorin.xseries.XSound;
import com.cryptomorin.xseries.messages.Titles;
import lombok.Getter;
import me.kasuki.kstaff.KStaffPlugin;
import me.kasuki.kstaff.api.constant.KStaffConstant;
import me.kasuki.kstaff.api.data.redis.Staffmessage;
import me.kasuki.kstaff.api.database.redis.event.EventOuterClass;
import me.kasuki.kstaff.api.database.redis.repository.stream.publisher.AbstractRedisStreamPublisher;
import me.kasuki.kstaff.api.profile.IProfileHandler;
import me.kasuki.kstaff.api.profile.wrapper.ProfileWrapper;
import me.kasuki.kstaff.utilities.chat.CC;
import me.kasuki.kstaff.utilities.chat.MessageUtil;
import me.kasuki.kstaff.utilities.config.ItemsConfig;
import me.kasuki.kstaff.utilities.config.LangConfig;
import me.kasuki.kstaff.utilities.config.MainConfig;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


public class StaffManager {
    private final KStaffPlugin instance;
    private final IProfileHandler profileHandler;

    @Getter
    public Map<UUID, String> lastRTPCache;

    public StaffManager(KStaffPlugin instance) {
        this.instance = instance;
        this.profileHandler = instance.getKStaffAPI().get(IProfileHandler.class);
        this.lastRTPCache = new HashMap<>();
    }

    /**
     * Staff Toggles
     */
    public void toggleStaffMode(Player player, ProfileWrapper wrapper, boolean enteringStaffMode) {
        wrapper = wrapper.setStaffModeState(enteringStaffMode);
        wrapper = this.handleInventories(player, wrapper, enteringStaffMode);
        wrapper.setChanged(true);
        this.profileHandler.addToCache(wrapper);

        if (enteringStaffMode) {
            MessageUtil.sendPrefixedMessage(player, LangConfig.STAFFMODE_ENABLED);
            player.playSound(player.getLocation(), XSound.BLOCK_NOTE_BLOCK_PLING.get(), 1, 1.3f);
            Titles.sendTitle(player, 10, 20, 10, CC.chat(LangConfig.STAFF_MODE_ENABLED_TITLE_MAIN), CC.chat(LangConfig.STAFF_MODE_ENABLED_TITLE_SUB));
            return;
        }

        this.instance.getScoreboardProviderManager().reset(player);
        MessageUtil.sendPrefixedMessage(player, LangConfig.STAFFMODE_DISABLED);
        player.playSound(player.getLocation(), XSound.BLOCK_NOTE_BLOCK_PLING.get(), 1, 0.6f);
        Titles.sendTitle(player, 10, 20, 10, CC.chat(LangConfig.STAFF_MODE_DISABLED_TITLE_MAIN), CC.chat(LangConfig.STAFF_MODE_DISABLED_TITLE_SUB));
    }

    public void toggleStaffChat(Player player, ProfileWrapper wrapper, boolean newState) {
        wrapper = wrapper.setStaffChatState(newState);
        wrapper.setChanged(true);
        this.profileHandler.addToCache(wrapper);

        if (newState) {
            MessageUtil.sendPrefixedMessage(player, LangConfig.STAFFCHAT_ENABLED);
            player.playSound(player.getLocation(), XSound.BLOCK_NOTE_BLOCK_PLING.get(), 1, 1.3f);
            return;
        }

        MessageUtil.sendPrefixedMessage(player, LangConfig.STAFFCHAT_DISABLED);
        player.playSound(player.getLocation(), XSound.BLOCK_NOTE_BLOCK_PLING.get(), 1, 0.6f);
    }

    /**
     * Staff chat messaging
     */
    public void publishStaffChatMessage(String message, String senderName){
        Staffmessage.StaffMessage staffMessage = Staffmessage.StaffMessage.newBuilder()
                .setMessage(message)
                .setSenderName(senderName)
                .setServerFrom(MainConfig.SERVER_NAME)
                .build();

        EventOuterClass.Event protoEvent = EventOuterClass.Event.newBuilder()
                .setEventType(EventOuterClass.EventType.STAFF_CHAT_MESSAGE)
                .setEventData(staffMessage.toByteString())
                .build();

        AbstractRedisStreamPublisher publisher = this.instance.getRedisStreamPublisher();
        publisher.publish(protoEvent, KStaffConstant.STAFF_CHAT_REDIS_KEY);
    }

    /**
     * Inventory save/load handling
     */
    private ProfileWrapper handleInventories(Player player, ProfileWrapper wrapper, boolean enteringStaffMode) {
        if (enteringStaffMode) {
            wrapper = wrapper.setSavedArmor(player.getInventory().getArmorContents())
                    .setSavedInventory(player.getInventory().getContents());
            player.getInventory().clear();
            player.getInventory().setArmorContents(new ItemStack[4]);
            this.populateStaffInventory(player);
            return wrapper;
        }

        player.getInventory().clear();
        wrapper.getSavedArmor().ifPresent(player.getInventory()::setArmorContents);
        wrapper.getSavedInventory().ifPresent(player.getInventory()::setContents);
        return wrapper.setSavedArmor(null).setSavedInventory(null);
    }

    /**
     * Helper
     */
    private void populateStaffInventory(Player player) {
        player.getInventory().setItem(
                ItemsConfig.RANDOM_TELEPORT_SLOT,
                this.instance.getItemManager().getFromId("RANDOM_TELEPORT").getItem()
        );
    }
}
