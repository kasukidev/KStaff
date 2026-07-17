package me.kasuki.kstaff.staff.scoreboard.task;

import me.kasuki.kstaff.KStaffPlugin;
import me.kasuki.kstaff.api.profile.IProfileHandler;
import me.kasuki.kstaff.staff.scoreboard.ScoreboardProviderManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.UUID;

public class ScoreboardTask extends BukkitRunnable {
    private final KStaffPlugin instance;
    private final ScoreboardProviderManager providerManager;
    private final IProfileHandler profileHandler;

    public ScoreboardTask(KStaffPlugin instance) {
        this.instance = instance;
        this.providerManager = this.instance.getKStaffAPI().get(ScoreboardProviderManager.class);
        this.profileHandler = this.instance.getKStaffAPI().get(IProfileHandler.class);

        this.runTaskTimer(this.instance, 20L, 20L);
    }

    /**
     * Tick active scoreboard provider
     */
    @Override
    public void run() {
        // Iter through staff players

        for (UUID uuid : this.instance.getStaffPlayers()) {
            // Get player obj
            Player player = Bukkit.getPlayer(uuid);
            if (player == null) continue;
            if(!profileHandler.isInStaffMode(player.getUniqueId())) return;


            this.providerManager.update(player);
        }
    }
}
