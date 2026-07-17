package me.kasuki.kstaff.staff.scoreboard.impl;

import com.decimatemc.decimatecommons.scoreboard.DemonicScoreboard;
import fr.minuskube.netherboard.Netherboard;
import fr.minuskube.netherboard.bukkit.BPlayerBoard;
import me.kasuki.kstaff.KStaffPlugin;
import me.kasuki.kstaff.staff.scoreboard.model.AbstractScoreboardProvider;
import me.kasuki.kstaff.utilities.chat.CC;
import me.kasuki.kstaff.utilities.config.LangConfig;
import me.kasuki.kstaff.utilities.date.DateUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.List;

public class DecimateProvider extends AbstractScoreboardProvider {
    public DecimateProvider(KStaffPlugin instance) {
        super(instance);
    }

    /**
     * Availability
     */
    @Override
    public int getPriority() {
        return 1;
    }

    @Override
    public boolean isPluginAvailable() {
        Plugin plugin = Bukkit.getPluginManager().getPlugin("DecimateCommons");
        return plugin != null && plugin.isEnabled();
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
        BPlayerBoard board = DemonicScoreboard.getOrCreateBoard(player, this.getTitle(player));
        List<String> resolved = this.applyPlaceholders(player, this.getLines(player));
        board.setAll(resolved.stream().map(CC::chat).toArray(String[]::new));
    }

    /**
     * Cleanup
     */
    @Override
    public void removePlayer(Player player) {
        if (Netherboard.instance().hasBoard(player)) {
            Netherboard.instance().getBoard(player).delete();

        }
    }
}
