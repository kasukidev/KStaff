package me.kasuki.kstaff.registration.gameplay;

import cc.insidious.fethmusmioma.CommandHandler;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import me.kasuki.kstaff.KStaffPlugin;
import me.kasuki.kstaff.api.registration.IRegistrationHandler;

/**
 * Handles command registration for this module.
 */
@RequiredArgsConstructor
public class CommandRegistrationHandler implements IRegistrationHandler {

    private final KStaffPlugin instance;

    @Override
    public void registerObjects() {
        CommandHandler commandHandler = new CommandHandler(this.instance, "kstaff");

        Stream.of().forEach(commandHandler::registerCommand);
    }
}
