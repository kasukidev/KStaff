package me.kasuki.kstaff.staff.scoreboard.model;

import me.kasuki.kstaff.KStaffPlugin;
import me.kasuki.kstaff.api.profile.IProfileHandler;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractScoreboardProvider {
    protected final KStaffPlugin instance;
    protected final IProfileHandler profileHandler;

    public AbstractScoreboardProvider(KStaffPlugin instance) {
        this.instance = instance;
        this.profileHandler = this.instance.getKStaffAPI().get(IProfileHandler.class);
    }

    /**
     * Provider metadata
     */
    public abstract int getPriority();
    public abstract boolean isPluginAvailable();

    /**
     * Scoreboard content
     */
    public abstract String getTitle(Player player);
    public abstract List<String> getLines(Player player);

    /**
     * Lifecycle
     */
    public abstract void update(Player player);
    public abstract void removePlayer(Player player);

    public boolean isApplicable(Player player) {
        return true;
    }

    /**
     * Placeholder replacement
     */
    protected List<String> applyPlaceholders(Player player, List<String> lines) {
        String staffChatState = this.profileHandler.isInStaffChat(player.getUniqueId()) ? "&aENABLED" : "&cDISABLED";
        String lastRtp = this.instance.getStaffManager().getLastRTPCache()
                .getOrDefault(player.getUniqueId(), "None");

        List<String> resolved = new ArrayList<>(lines.size());
        for (String line : lines) {
            resolved.add(line
                    .replace("%player_name%", player.getName())
                    .replace("%staff_chat_state%", staffChatState)
                    .replace("%last_rtp_name%", lastRtp));
        }
        return resolved;
    }
}
