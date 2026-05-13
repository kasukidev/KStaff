package me.kasuki.kstaff.registration.gameplay;

import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import me.kasuki.kstaff.KStaffPlugin;
import me.kasuki.kstaff.api.registration.IRegistrationHandler;
import me.kasuki.kstaff.data.profile.listener.ProfileListener;
import me.kasuki.kstaff.item.listener.ItemListener;
import me.kasuki.kstaff.staff.listener.StaffChatListener;
import me.kasuki.kstaff.staff.listener.StaffModeListener;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;

/**
 * Handles listener registration for this module.
 */
@RequiredArgsConstructor
public class ListenerRegistrationHandler implements IRegistrationHandler {
    private final KStaffPlugin instance;

    @Override
    public void registerObjects() {
        PluginManager manager = this.instance.getServer().getPluginManager();
        Stream.of(new ProfileListener(this.instance),
                        new ItemListener(this.instance),
                        new StaffModeListener(this.instance),
                        new StaffChatListener(this.instance))
                .filter(Listener.class::isInstance)
                .map(Listener.class::cast)
                .forEach(listener -> manager.registerEvents(listener, this.instance));
    }
}
