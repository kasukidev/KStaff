package me.kasuki.kstaff.save;

import me.kasuki.kstaff.KStaffPlugin;
import me.kasuki.kstaff.api.profile.IProfileHandler;
import me.kasuki.kstaff.profile.util.ProfileSaveUtil;
import org.bukkit.scheduler.BukkitRunnable;

// Leaving room for further expansion
public class SaveRunnable extends BukkitRunnable {
    private final IProfileHandler profileHandler;

    public SaveRunnable(KStaffPlugin instance) {
        this.profileHandler = instance.getKStaffAPI().get(IProfileHandler.class);

        long delay = 20 * 30L;
        this.runTaskTimer(instance, delay, delay);
    }

    @Override
    public void run() {
        this.saveProfiles();
    }

    private void saveProfiles() {
        if (this.profileHandler == null) {
            return;
        }

        ProfileSaveUtil.processProfileSaving(profileHandler);
    }
}
