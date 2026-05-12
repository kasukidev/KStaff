package me.kasuki.kstaff.staff;

import com.cryptomorin.xseries.XSound;
import me.kasuki.kstaff.KStaffPlugin;
import me.kasuki.kstaff.api.profile.IProfileHandler;
import me.kasuki.kstaff.api.profile.wrapper.ProfileWrapper;
import me.kasuki.kstaff.utilities.chat.MessageUtil;
import me.kasuki.kstaff.utilities.config.ItemsConfig;
import me.kasuki.kstaff.utilities.config.LangConfig;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class StaffModeManager {
    private final KStaffPlugin instance;
    private final IProfileHandler profileHandler;

    public StaffModeManager(KStaffPlugin instance) {
        this.instance = instance;
        this.profileHandler = instance.getKStaffAPI().get(IProfileHandler.class);
    }

    /**
     * Main method
     */
    public void toggleStaffMode(Player player, ProfileWrapper wrapper, boolean enteringStaffMode) {
        wrapper = wrapper.setStaffModeState(enteringStaffMode);
        wrapper = this.handleInventories(player, wrapper, enteringStaffMode);
        wrapper.setChanged(true);
        this.profileHandler.addToCache(wrapper);

        if (enteringStaffMode) {
            MessageUtil.sendPrefixedMessage(player, LangConfig.STAFFMODE_ENABLED);
            player.playSound(player.getLocation(), XSound.BLOCK_NOTE_BLOCK_PLING.get(), 1, 1.3f);
            return;
        }

        MessageUtil.sendPrefixedMessage(player, LangConfig.STAFFMODE_DISABLED);
        player.playSound(player.getLocation(), XSound.BLOCK_NOTE_BLOCK_PLING.get(), 1, 0.6f);
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
