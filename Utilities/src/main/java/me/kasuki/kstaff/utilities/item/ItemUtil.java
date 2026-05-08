package me.kasuki.kstaff.utilities.item;

import com.cryptomorin.xseries.XMaterial;
import lombok.experimental.UtilityClass;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

/**
 * Utility methods for item operations.
 */
@UtilityClass
public class ItemUtil {

    /**
     * Executes give item.
     */
    public void giveItem(Player player, ItemStack stack) {
        if (XMaterial.matchXMaterial(stack) == XMaterial.AIR) {
            return;
        }

        PlayerInventory inventory = player.getInventory();
        if (inventory.firstEmpty() != -1) {
            inventory.addItem(stack);
            return;
        }
        Location playerLocation = player.getLocation();
        World world = playerLocation.getWorld();
        world.dropItem(playerLocation, stack);
    }
}
