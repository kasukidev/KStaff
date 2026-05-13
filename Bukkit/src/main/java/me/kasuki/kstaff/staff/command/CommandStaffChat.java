package me.kasuki.kstaff.staff.command;

import cc.insidious.fethmusmioma.annotation.Command;
import com.cryptomorin.xseries.XSound;
import me.kasuki.kstaff.KStaffPlugin;
import me.kasuki.kstaff.api.profile.IProfileHandler;
import me.kasuki.kstaff.api.profile.wrapper.ProfileWrapper;
import me.kasuki.kstaff.staff.StaffManager;
import me.kasuki.kstaff.utilities.chat.MessageUtil;
import me.kasuki.kstaff.utilities.config.LangConfig;
import org.bukkit.entity.Player;

public class CommandStaffChat {
    private final KStaffPlugin instance;
    private final IProfileHandler profileHandler;

    public CommandStaffChat(KStaffPlugin instance) {
        this.instance = instance;
        this.profileHandler = this.instance.getKStaffAPI().get(IProfileHandler.class);
    }

    @Command(label = "staffchat", aliases = {"sc", "mc", "modchat"}, permission = "kstaff.staffchat")
    public void executeStaffChat(Player player){
        ProfileWrapper wrapper = this.profileHandler.getFromCache(player.getUniqueId()).orElse(null);
        if (wrapper == null) {
            MessageUtil.sendPrefixedMessage(player, LangConfig.PROFILE_NOT_FOUND);
            player.playSound(player.getLocation(), XSound.BLOCK_LAVA_POP.get(), 1, 1);
            return;
        }

        StaffManager staffManager = this.instance.getStaffManager();
        staffManager.toggleStaffChat(player, wrapper, !wrapper.isInStaffChat());
    }
}
