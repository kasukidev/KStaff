package me.kasuki.kstaff.staff.scoreboard;

import me.kasuki.kstaff.KStaffPlugin;
import me.kasuki.kstaff.staff.scoreboard.impl.DecimateProvider;
import me.kasuki.kstaff.staff.scoreboard.impl.TABProvider;
import me.kasuki.kstaff.staff.scoreboard.model.AbstractScoreboardProvider;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ScoreboardProviderManager {
    private final KStaffPlugin instance;

    private final List<AbstractScoreboardProvider> providers = new ArrayList<>();
    private AbstractScoreboardProvider activeProvider;

    public ScoreboardProviderManager(KStaffPlugin instance) {
        this.instance = instance;
    }

    /**
     * Registration
     */
    public void register(AbstractScoreboardProvider provider) {
        this.providers.add(provider);
        this.providers.sort(Comparator.comparingInt(AbstractScoreboardProvider::getPriority).reversed());
    }

    /**
     * Updates
     */
    public void update(Player player) {
        if (this.activeProvider == null) return;
        if (!this.activeProvider.isApplicable(player)) return;
        this.activeProvider.update(player);
    }

    public void reset(Player player) {
        if (this.activeProvider == null) return;
        this.activeProvider.removePlayer(player);
    }

    /**
     * Initialization
     */
    public void init() {
        this.register(new TABProvider(this.instance));
        this.register(new DecimateProvider(this.instance));

        // Walk providers by priority and pick the first whose plugin is present
        this.activeProvider = this.providers.stream()
                .filter(AbstractScoreboardProvider::isPluginAvailable)
                .findFirst()
                .orElse(null);

        if (this.activeProvider != null) {
            this.instance.getLogger().info("Scoreboard provider: " + this.activeProvider.getClass().getSimpleName());
            return;
        }

        this.instance.getLogger().warning("No scoreboard plugin found. Scoreboard logic disabled.");
    }
}
