package me.kasuki.kstaff.registration.gameplay;

import cc.insidious.fethmusmioma.CommandHandler;
import lombok.RequiredArgsConstructor;
import me.kasuki.kstaff.KStaffPlugin;
import me.kasuki.kstaff.api.registration.IRegistrationHandler;
import me.kasuki.kstaff.staff.command.CommandStaffChat;
import me.kasuki.kstaff.staff.command.CommandStaffMode;

import java.util.stream.Stream;

/**
 * Handles command registration for this module.
 */
@RequiredArgsConstructor
public class CommandRegistrationHandler implements IRegistrationHandler {

    private final KStaffPlugin instance;

    @Override
    public void registerObjects() {
        CommandHandler commandHandler = new CommandHandler(this.instance, "kstaff");

        Stream.of(new CommandStaffMode(this.instance), new CommandStaffChat(this.instance)).forEach(commandHandler::registerCommand);
    }
}
