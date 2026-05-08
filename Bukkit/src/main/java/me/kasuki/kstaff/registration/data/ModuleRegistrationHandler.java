package me.kasuki.kstaff.registration.data;

import lombok.RequiredArgsConstructor;
import me.kasuki.kstaff.KStaffPlugin;
import me.kasuki.kstaff.api.KStaffAPI;
import me.kasuki.kstaff.api.registration.IRegistrationHandler;

/**
 * Handles module registration for this module.
 */
@RequiredArgsConstructor
public class ModuleRegistrationHandler implements IRegistrationHandler {
    private final KStaffPlugin instance;

    private final KStaffAPI KStaffAPI;

    @Override
    public void registerObjects() {
    }
}
