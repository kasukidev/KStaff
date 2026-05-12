package me.kasuki.kstaff.item.impl;

import com.cryptomorin.xseries.XMaterial;
import com.cryptomorin.xseries.XSound;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import me.kasuki.kstaff.KStaffPlugin;
import me.kasuki.kstaff.item.AbstractItem;
import me.kasuki.kstaff.utilities.chat.MessageUtil;
import me.kasuki.kstaff.utilities.config.ItemsConfig;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public class RandomTPItem extends AbstractItem {
    public RandomTPItem(KStaffPlugin instance) {
        super(instance);
    }

    @Override
    public String getId() {
        return "RANDOM_TELEPORT";
    }

    @Override
    public String getDisplayName() {
        return ItemsConfig.RANDOM_TELEPORT_ITEM_NAME;
    }

    @Override
    public List<String> getLore() {
        return ItemsConfig.RANDOM_TELEPORT_ITEM_LORE;
    }

    @Override
    public XMaterial getMaterial() {
        Optional<XMaterial> material = XMaterial.matchXMaterial(ItemsConfig.RANDOM_TELEPORT_MATERIAL);
        return material.orElse(XMaterial.ARROW);
    }


    @Override
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        List<Player> onlinePlayers = new ArrayList<>(this.instance.getServer().getOnlinePlayers());
        onlinePlayers.remove(player);

        if(onlinePlayers.isEmpty()){
            MessageUtil.sendPrefixedMessage(player, ItemsConfig.RANDOM_TELEPORT_NO_ONLINE_PLAYERS);
            player.playSound(player.getLocation(), XSound.BLOCK_NOTE_BLOCK_PLING.get(), 1, 1);
            return;
        }

        Player randomPlayer = onlinePlayers.get(ThreadLocalRandom.current().nextInt(onlinePlayers.size()));
        player.teleport(randomPlayer.getLocation(), PlayerTeleportEvent.TeleportCause.PLUGIN);
        player.playSound(player.getLocation(), XSound.ENTITY_EXPERIENCE_ORB_PICKUP.get(), 1, 1);
        MessageUtil.sendPrefixedMessage(player, ItemsConfig.RANDOM_TELEPORT_SUCCESS.replace("%player%", randomPlayer.getName()));
    }
}
