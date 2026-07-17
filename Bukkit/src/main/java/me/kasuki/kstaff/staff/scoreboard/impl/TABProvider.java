package me.kasuki.kstaff.staff.scoreboard.impl;

import me.kasuki.kstaff.KStaffPlugin;
import me.kasuki.kstaff.staff.scoreboard.model.AbstractScoreboardProvider;
import me.kasuki.kstaff.utilities.config.LangConfig;
import me.kasuki.kstaff.utilities.date.DateUtil;
import me.neznamy.tab.api.TabAPI;
import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.api.scoreboard.Scoreboard;
import me.neznamy.tab.api.scoreboard.ScoreboardManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class TABProvider extends AbstractScoreboardProvider {
    private final Map<UUID, Scoreboard> playerBoards = new HashMap<>();

    public TABProvider(KStaffPlugin instance) {
        super(instance);
    }

    /**
     * Availability
     */
    @Override
    public int getPriority() {
        return 0;
    }

    @Override
    public boolean isPluginAvailable() {
        Plugin tabPlugin = Bukkit.getPluginManager().getPlugin("TAB");
        if (tabPlugin == null || !tabPlugin.isEnabled()) return false;

        return TabAPI.getInstance().getScoreboardManager() != null;
    }

    /**
     * Scoreboard content
     */
    @Override
    public String getTitle(Player player) {
        return LangConfig.SCOREBOARD_TITLE.replace("%simple_date_format%", DateUtil.getDate());
    }

    @Override
    public List<String> getLines(Player player) {
        return LangConfig.SCOREBOARD_LINES;
    }

    /**
     * Rendering
     */
    @Override
    public void update(Player player) {
        TabPlayer tabPlayer = TabAPI.getInstance().getPlayer(player.getUniqueId());
        if (tabPlayer == null) return;

        ScoreboardManager sbManager = TabAPI.getInstance().getScoreboardManager();
        if (sbManager == null) return;

        // Create board for this player if not already cached
        String boardName = "kstaff_" + player.getUniqueId().toString().replace("-", "").substring(0, 12);
        Scoreboard scoreboard = this.playerBoards.computeIfAbsent(
                player.getUniqueId(),
                uuid -> sbManager.createScoreboard(boardName, this.getTitle(player), this.getLines(player))
        );

        List<String> resolved = this.applyPlaceholders(player, this.getLines(player));

        scoreboard.setTitle(this.getTitle(player));

        // Rebuild lines from the end to avoid index shifting on removal
        for (int i = scoreboard.getLines().size() - 1; i >= 0; i--) {
            scoreboard.removeLine(i);
        }
        for (String line : resolved) {
            scoreboard.addLine(line);
        }

        sbManager.showScoreboard(tabPlayer, scoreboard);
    }

    /**
     * Cleanup
     */
    @Override
    public void removePlayer(Player player) {
        this.playerBoards.remove(player.getUniqueId());

        TabPlayer tabPlayer = TabAPI.getInstance().getPlayer(player.getUniqueId());
        if (tabPlayer == null) return;

        ScoreboardManager sbManager = TabAPI.getInstance().getScoreboardManager();
        if (sbManager == null) return;

        sbManager.resetScoreboard(tabPlayer);
    }
}
