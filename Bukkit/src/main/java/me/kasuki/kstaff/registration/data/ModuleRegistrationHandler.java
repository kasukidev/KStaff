package me.kasuki.kstaff.registration.data;

import lombok.RequiredArgsConstructor;
import me.kasuki.kstaff.KStaffPlugin;
import me.kasuki.kstaff.api.KStaffAPI;
import me.kasuki.kstaff.api.profile.IProfileHandler;
import me.kasuki.kstaff.api.registration.IRegistrationHandler;
import me.kasuki.kstaff.profile.SQLiteProfileHandler;
import me.kasuki.kstaff.utilities.pair.Pair;

import java.util.stream.Stream;

/**
 * Handles module registration for this module.
 */
@RequiredArgsConstructor
public class ModuleRegistrationHandler implements IRegistrationHandler {
    private final KStaffPlugin instance;

    private final KStaffAPI kStaffAPI;

    @Override
    public void registerObjects() {
        Stream.of(Pair.from(IProfileHandler.class, new SQLiteProfileHandler(this.instance)))
                .forEachOrdered(pair -> this.kStaffAPI.register(pair.getKey(), pair.getValue()));

    }
}
