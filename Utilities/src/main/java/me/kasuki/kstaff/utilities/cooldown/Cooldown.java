package me.kasuki.kstaff.utilities.cooldown;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Cooldown {

    private final Map<UUID, Long> cooldownMap = new HashMap<>();

    public boolean isActive(UUID uniqueId) {
        return this.cooldownMap.containsKey(uniqueId) && this.cooldownMap.get(uniqueId) > System.currentTimeMillis();
    }

    public void placeOnCooldown(UUID uniqueId, long duration) {
        this.cooldownMap.put(uniqueId, (System.currentTimeMillis() + duration));
    }

    public void removeCooldown(UUID uniqueId) {
        this.cooldownMap.remove(uniqueId);
    }

    public long getRemaining(UUID uniqueId) {
        return this.cooldownMap.get(uniqueId) - System.currentTimeMillis();
    }
}
