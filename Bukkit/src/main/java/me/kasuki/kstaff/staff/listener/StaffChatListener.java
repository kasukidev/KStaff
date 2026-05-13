package me.kasuki.kstaff.staff.listener;

import java.util.Set;
import java.util.UUID;
import me.kasuki.kstaff.KStaffPlugin;
import me.kasuki.kstaff.api.constant.KStaffConstant;
import me.kasuki.kstaff.api.data.redis.Staffmessage;
import me.kasuki.kstaff.api.database.redis.event.EventOuterClass;
import me.kasuki.kstaff.api.database.redis.repository.stream.publisher.AbstractRedisStreamPublisher;
import me.kasuki.kstaff.api.profile.IProfileHandler;
import me.kasuki.kstaff.api.profile.wrapper.ProfileWrapper;
import me.kasuki.kstaff.utilities.config.MainConfig;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class StaffChatListener implements Listener {
    private final KStaffPlugin instance;
    private final IProfileHandler profileHandler;

    public StaffChatListener(KStaffPlugin instance) {
        this.instance = instance;
        this.profileHandler = this.instance.getKStaffAPI().get(IProfileHandler.class);
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        ProfileWrapper profileWrapper = this.profileHandler.getFromCache(player.getUniqueId()).orElse(null);

        if (profileWrapper == null) return;
        if (!player.hasPermission("kstaff.staffchat")) return;
        if (!profileWrapper.isInStaffChat()) return;

        Staffmessage.StaffMessage staffMessage = Staffmessage.StaffMessage.newBuilder()
                .setMessage(event.getMessage())
                .setSenderName(player.getName())
                .setServerFrom(MainConfig.SERVER_NAME)
                .build();

        EventOuterClass.Event protoEvent = EventOuterClass.Event.newBuilder()
                .setEventType(EventOuterClass.EventType.STAFF_CHAT_MESSAGE)
                .setEventData(staffMessage.toByteString())
                .build();

        AbstractRedisStreamPublisher publisher = this.instance.getRedisStreamPublisher();
        publisher.publish(protoEvent, KStaffConstant.STAFF_CHAT_REDIS_KEY);
        event.setCancelled(true);
    }

    /**
     * Staff Player Cache
     * TODO: Implement permission removing checks
     */
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (!player.hasPermission("kstaff.staffchat")) return;

        this.instance.getStaffPlayers().add(player.getUniqueId());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        Set<UUID> staffPlayers = this.instance.getStaffPlayers();
        if (!staffPlayers.contains(player.getUniqueId())) return;

        staffPlayers.remove(player.getUniqueId());
    }


}
