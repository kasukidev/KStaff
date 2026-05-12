package me.kasuki.kstaff.data.profile.listener;

import me.kasuki.kstaff.KStaffPlugin;
import me.kasuki.kstaff.api.profile.IProfileHandler;
import me.kasuki.kstaff.api.profile.wrapper.ProfileWrapper;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;

import java.util.UUID;

public class ProfileListener implements Listener {

    private final IProfileHandler profileHandler;

    public ProfileListener(KStaffPlugin instance) {
        this.profileHandler = instance.getKStaffAPI().get(IProfileHandler.class);
    }

    @EventHandler
    public void onAsyncPlayerLogin(AsyncPlayerPreLoginEvent event) {
        UUID uuid = event.getUniqueId();

        if (this.profileHandler.getFromCache(uuid).isPresent()) {
            return;
        }

        this.profileHandler.getFromDatabase(
                uuid,
                optional -> {
                    if (optional.isPresent()) {
                        this.profileHandler.addToCache(optional.get());
                        return;
                    }

                    ProfileWrapper profileWrapper = ProfileWrapper.from(uuid).setChanged(true);
                    this.profileHandler.saveToDatabase(profileWrapper);
                    this.profileHandler.addToCache(profileWrapper);
                });
    }
}
