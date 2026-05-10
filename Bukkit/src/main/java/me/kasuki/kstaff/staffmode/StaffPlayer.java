package me.kasuki.kstaff.staffmode;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter @Setter
public class StaffPlayer {
    private final UUID uuid;

    public StaffPlayer(UUID uuid) {
        this.uuid = uuid;
    }
}
