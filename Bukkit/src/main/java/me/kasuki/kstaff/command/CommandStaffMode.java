package me.kasuki.kstaff.command;

import cc.insidious.fethmusmioma.annotation.Command;
import com.cryptomorin.xseries.XSound;
import me.kasuki.kstaff.KStaffPlugin;
import me.kasuki.kstaff.api.profile.IProfileHandler;
import me.kasuki.kstaff.api.profile.wrapper.ProfileWrapper;
import me.kasuki.kstaff.utilities.chat.MessageUtil;
import me.kasuki.kstaff.utilities.config.LangConfig;
import org.bukkit.entity.Player;

public class CommandStaffMode {
    private final KStaffPlugin instance;
    private final IProfileHandler profileHandler;

    public CommandStaffMode(KStaffPlugin instance) {
        this.instance = instance;
        this.profileHandler = this.instance.getKStaffAPI().get(IProfileHandler.class);
    }

    @Command(label = "staffmode", aliases = {"mod", "mm", "sm", "modmode", "staff"}, permission = "kstaff.staffmode")
    public void executeStaffMode(Player player){
        ProfileWrapper wrapper = this.profileHandler.getFromCache(player.getUniqueId()).orElse(null);
        if(wrapper == null){
            MessageUtil.sendPrefixedMessage(player, LangConfig.PROFILE_NOT_FOUND);
            player.playSound(player.getLocation(), XSound.BLOCK_LAVA_POP.get(), 1, 1);
            return;
        }


    }
}
