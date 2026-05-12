package me.kasuki.kstaff.registration.data;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.kasuki.kstaff.KStaffPlugin;
import me.kasuki.kstaff.api.registration.IRegistrationHandler;
import me.kasuki.kstaff.utilities.config.ItemsConfig;
import me.kasuki.kstaff.utilities.config.LangConfig;
import me.kasuki.kstaff.utilities.config.MainConfig;

/**
 * Handles config registration for this module.
 */
@RequiredArgsConstructor
@Getter
public class ConfigRegistrationHandler implements IRegistrationHandler {
    private final KStaffPlugin instance;

    private MainConfig mainConfig;
    private LangConfig langConfig;
    private ItemsConfig itemsConfig;

    @Override
    public void registerObjects() {
        this.mainConfig = new MainConfig(this.instance);
        this.langConfig = new LangConfig(this.instance);
        this.itemsConfig = new ItemsConfig(this.instance);
    }
}
