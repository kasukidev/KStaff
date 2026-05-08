package me.kasuki.kstaff.registration.data;

import lombok.RequiredArgsConstructor;
import me.kasuki.kstaff.KStaffPlugin;
import me.kasuki.kstaff.api.registration.IRegistrationHandler;

/**
 * Handles config registration for this module.
 */
@RequiredArgsConstructor
public class ConfigRegistrationHandler implements IRegistrationHandler {
    private final KStaffPlugin instance;

    @Override
    public void registerObjects() {
    }
}
