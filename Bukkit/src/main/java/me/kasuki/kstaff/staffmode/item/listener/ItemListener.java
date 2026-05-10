package me.kasuki.kstaff.staffmode.item.listener;

import com.cryptomorin.xseries.XMaterial;
import de.tr7zw.changeme.nbtapi.NBTItem;
import me.kasuki.kstaff.KStaffPlugin;
import me.kasuki.kstaff.staffmode.item.AbstractItem;
import me.kasuki.kstaff.utilities.cooldown.Cooldown;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class ItemListener implements Listener {
    private final KStaffPlugin instance;
    private final Cooldown cooldown;

    public ItemListener(KStaffPlugin instance) {
        this.instance = instance;
        this.cooldown = new Cooldown();
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event){
        Player player = event.getPlayer();
        ItemStack heldItem = player.getItemInHand();

        if(this.cooldown.isActive(player.getUniqueId())) return;
        if(heldItem == null || XMaterial.matchXMaterial(heldItem.getType()) == XMaterial.AIR) return;
        NBTItem nbtItem = new NBTItem(heldItem);
        if(!nbtItem.hasKey("itemType")) return;

        AbstractItem item = this.instance.getItemManager().getFromId(nbtItem.getString("itemType"));
        if(item == null) return;

        this.cooldown.placeOnCooldown(player.getUniqueId(), 100);
        item.onInteract(event);
        event.setCancelled(true);
    }
}
