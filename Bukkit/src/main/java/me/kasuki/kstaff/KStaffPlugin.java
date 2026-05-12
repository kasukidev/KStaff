package me.kasuki.kstaff;

import java.util.stream.Stream;
import lombok.Getter;
import me.kasuki.kstaff.api.KStaffAPI;
import me.kasuki.kstaff.api.registration.IRegistrationHandler;
import me.kasuki.kstaff.item.ItemManager;
import me.kasuki.kstaff.registration.data.ConfigRegistrationHandler;
import me.kasuki.kstaff.registration.data.ModuleRegistrationHandler;
import me.kasuki.kstaff.registration.gameplay.CommandRegistrationHandler;
import me.kasuki.kstaff.registration.gameplay.ListenerRegistrationHandler;
import me.kasuki.kstaff.save.SaveRunnable;
import org.bukkit.plugin.java.JavaPlugin;

@Getter
public class KStaffPlugin extends JavaPlugin {
    private KStaffAPI KStaffAPI;
    private SaveRunnable saveRunnable;
    private ItemManager itemManager;

    @Override
    public void onEnable() {
        new ConfigRegistrationHandler(this).registerObjects();
        this.KStaffAPI = new KStaffAPI(this.getLogger());

        Stream.of(
                        new ModuleRegistrationHandler(this, this.KStaffAPI),
                        new CommandRegistrationHandler(this),
                        new ListenerRegistrationHandler(this))
                .forEachOrdered(IRegistrationHandler::registerObjects);

        this.registerItemManager();
        this.saveRunnable = new SaveRunnable(this);
    }

    @Override
    public void onDisable() {
        if (this.saveRunnable != null) {
            this.saveRunnable.cancel();
            this.saveRunnable = null;
        }

        this.KStaffAPI.shutdown();
    }

    private void registerItemManager(){
        this.itemManager = new ItemManager(this);
        this.itemManager.init();
    }
}
