package me.kasuki.kstaff.staff.command;

import cc.insidious.fethmusmioma.annotation.Command;
import com.cryptomorin.xseries.XSound;
import me.kasuki.kstaff.KStaffPlugin;
import me.kasuki.kstaff.api.profile.IProfileHandler;
import me.kasuki.kstaff.api.profile.wrapper.ProfileWrapper;
import me.kasuki.kstaff.item.ItemManager;
import me.kasuki.kstaff.utilities.chat.MessageUtil;
import me.kasuki.kstaff.utilities.config.ItemsConfig;
import me.kasuki.kstaff.utilities.config.LangConfig;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class CommandStaffMode {
    private final KStaffPlugin instance;
    private final IProfileHandler profileHandler;

    public CommandStaffMode(KStaffPlugin instance) {
        this.instance = instance;
        this.profileHandler = this.instance.getKStaffAPI().get(IProfileHandler.class);
    }

    /**
     * Core staffmode command
     */
    @Command(label = "staffmode", aliases = {"mod", "mm", "sm", "modmode", "staff"}, permission = "kstaff.staffmode")
    public void executeStaffMode(Player player){
        ProfileWrapper wrapper = this.profileHandler.getFromCache(player.getUniqueId()).orElse(null);
        if(wrapper == null){
            MessageUtil.sendPrefixedMessage(player, LangConfig.PROFILE_NOT_FOUND);
            player.playSound(player.getLocation(), XSound.BLOCK_LAVA_POP.get(), 1, 1);
            return;
        }

        boolean newStaffState = !wrapper.isInStaffMode();
        wrapper = wrapper.setStaffModeState(newStaffState).setStaffChatState(true);
        wrapper = this.handleArmorUpdate(player, wrapper, newStaffState);
        this.profileHandler.addToCache(wrapper);

        if(!newStaffState){
            MessageUtil.sendPrefixedMessage(player, LangConfig.STAFFMODE_DISABLED);
            player.playSound(player.getLocation(), XSound.BLOCK_NOTE_BLOCK_PLING.get(), 1, 0.6f);
            return;
        }

        MessageUtil.sendPrefixedMessage(player, LangConfig.STAFFMODE_ENABLED);
        player.playSound(player.getLocation(), XSound.BLOCK_NOTE_BLOCK_PLING.get(), 1, 1.3f);
        return;
    }


    /**
     * Update saved armor & inventories
     */
    public ProfileWrapper handleArmorUpdate(Player player, ProfileWrapper wrapper, boolean enteringStaffMode) {
        if (enteringStaffMode) {
            wrapper = wrapper
                    .setSavedArmor(player.getInventory().getArmorContents())
                    .setSavedInventory(player.getInventory().getContents());

            player.getInventory().clear();
            player.getInventory().setArmorContents(new ItemStack[4]);
            this.populateStaffInventory(player);
            return wrapper;
        }

        player.getInventory().clear();
        wrapper.getSavedArmor().ifPresent(player.getInventory()::setArmorContents);
        wrapper.getSavedInventory().ifPresent(player.getInventory()::setContents);

        return wrapper
                .setSavedArmor(null)
                .setSavedInventory(null);
    }

    /**
     * Populate player inventory
     */
    public void populateStaffInventory(Player player){
        ItemManager itemManager = this.instance.getItemManager();
        player.getInventory().setItem(ItemsConfig.RANDOM_TELEPORT_SLOT, itemManager.getFromId("RANDOM_TELEPORT").getItem());
    }
}
