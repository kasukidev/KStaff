package me.kasuki.kstaff.item.listener;

import com.cryptomorin.xseries.XMaterial;
import de.tr7zw.changeme.nbtapi.NBTItem;
import me.kasuki.kstaff.KStaffPlugin;
import me.kasuki.kstaff.item.AbstractItem;
import me.kasuki.kstaff.utilities.cooldown.Cooldown;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
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

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack heldItem = event.getItem();

        if (heldItem == null || heldItem.getType().equals(XMaterial.AIR.get())) return;

        NBTItem nbtItem = new NBTItem(heldItem);
        if (!nbtItem.hasKey("itemType")) return;

        AbstractItem item = instance.getItemManager().getFromId(nbtItem.getString("itemType"));
        if (item == null) return;
        if (cooldown.isActive(player.getUniqueId())) return;

        cooldown.placeOnCooldown(player.getUniqueId(), 100);
        item.onInteract(event);
        event.setCancelled(true);
    }
}
