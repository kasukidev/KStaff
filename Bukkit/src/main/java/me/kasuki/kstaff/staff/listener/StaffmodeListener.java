package me.kasuki.kstaff.staff.listener;

import me.kasuki.kstaff.KStaffPlugin;
import me.kasuki.kstaff.api.profile.IProfileHandler;
import me.kasuki.kstaff.api.profile.wrapper.ProfileWrapper;
import me.kasuki.kstaff.utilities.chat.MessageUtil;
import me.kasuki.kstaff.utilities.config.LangConfig;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerExpChangeEvent;

public class StaffmodeListener implements Listener {
    private final KStaffPlugin instance;
    private final IProfileHandler profileHandler;

    public StaffmodeListener(KStaffPlugin instance) {
        this.instance = instance;
        this.profileHandler = this.instance.getKStaffAPI().get(IProfileHandler.class);
    }

    /**
     * Disable PvP Incoming & Outgoing
     */
    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        if (!(event.getDamager() instanceof Player)) return;

        Player victim = (Player) event.getEntity();
        Player attacker = (Player) event.getDamager();

        if (this.isInStaffMode(victim) || this.isInStaffMode(attacker)) {
            event.setCancelled(true);
        }
    }

    /**
     * Disable Incoming general DMG
     */
    @EventHandler
    public void onEntityDamageByEntity(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;

        Player player = (Player) event.getEntity();
        if (!this.isInStaffMode(player)) return;
        event.setCancelled(true);
    }


    /**
     * Projectile throwing
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onProjectileLaunch(ProjectileLaunchEvent event) {
        if (!(event.getEntity().getShooter() instanceof Player)) return;

        Player player = (Player) event.getEntity().getShooter();
        if (!isInStaffMode(player)) return;
        event.setCancelled(true);
    }

    /**
     * Disable inventory movements
     */
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;

        Player player = (Player) event.getWhoClicked();
        if (!this.isInStaffMode(player)) return;
        event.setCancelled(true);
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;

        Player player = (Player) event.getWhoClicked();
        if (!this.isInStaffMode(player)) return;
        event.setCancelled(true);
    }

    /**
     * Disable block placement
     */
    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event){
        Player player = event.getPlayer();
        if(!this.isInStaffMode(player)) return;

        MessageUtil.sendPrefixedMessage(player, LangConfig.NO_PLACING_BLOCKS);
        event.setCancelled(true);
    }

    /**
     * Disable block breaking
     */
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event){
        Player player = event.getPlayer();
        if(!this.isInStaffMode(player)) return;

        MessageUtil.sendPrefixedMessage(player, LangConfig.NO_BREAKING_BLOCKS);
        event.setCancelled(true);
    }

    /**
     * Disable hunger loss
     */
    @EventHandler
    public void onHungerLoss(FoodLevelChangeEvent event){
        if(!(event.getEntity() instanceof Player)) return;

        Player player = (Player) event.getEntity();
        if(!this.isInStaffMode(player)) return;

        event.setCancelled(true);
        player.setFoodLevel(20);
    }

    /**
     * Bed Check (Lol)
     */
    @EventHandler
    public void onBedEnter(PlayerBedEnterEvent event){
        Player player =  event.getPlayer();
        if(!this.isInStaffMode(player)) return;

        event.setCancelled(true);
    }

    /**
     * EXP Change
     */
    @EventHandler
    public void onEXPChange(PlayerExpChangeEvent event){
        Player player =  event.getPlayer();
        if(!this.isInStaffMode(player)) return;

        event.setAmount(0);
    }

    /**
     * Helper Method
     */
    public boolean isInStaffMode(Player player){
        return this.profileHandler
                .getFromCache(player.getUniqueId())
                .map(ProfileWrapper::isInStaffMode)
                .orElse(false);
    }
}
