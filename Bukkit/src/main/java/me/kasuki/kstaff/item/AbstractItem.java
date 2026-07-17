package me.kasuki.kstaff.item;

import com.cryptomorin.xseries.XMaterial;
import de.tr7zw.changeme.nbtapi.NBTItem;
import me.kasuki.kstaff.KStaffPlugin;
import me.kasuki.kstaff.utilities.item.ItemBuilder;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public abstract class AbstractItem {
    protected final KStaffPlugin instance;

    public AbstractItem(KStaffPlugin instance) {
        this.instance = instance;
    }

    public abstract String getId();
    public abstract String getDisplayName();
    public abstract List<String> getLore();
    public abstract XMaterial getMaterial();

    public ItemStack getItem(){
        ItemBuilder builder = new ItemBuilder(this.getMaterial())
                .setLore(this.getLore())
                .setName(this.getDisplayName());
        NBTItem nbtItem = new NBTItem(builder.build());
        nbtItem.setString("itemType", this.getId());

        return nbtItem.getItem();
    }


    public void onInteract(PlayerInteractEvent event) {

    }
}
