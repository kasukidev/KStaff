package me.kasuki.kstaff.utilities.location;

import lombok.experimental.UtilityClass;
import org.bukkit.Bukkit;
import org.bukkit.Location;

/**
 * Utility methods for location operations.
 */
@UtilityClass
public class LocationUtil {

    /**
     * Creates from bukkit.
     */
    public LocationOuterClass.Location fromBukkit(Location location) {
        return LocationOuterClass.Location.newBuilder()
                .setWorldId(location.getWorld().getName())
                .setX(location.getBlockX())
                .setY(location.getBlockY())
                .setZ(location.getBlockZ())
                .setYaw(location.getYaw())
                .setPitch(location.getPitch())
                .build();
    }

    /**
     * Converts to bukkit.
     */
    public Location toBukkit(LocationOuterClass.Location location) {
        return new Location(
                Bukkit.getServer().getWorld(location.getWorldId()),
                location.getX(),
                location.getY(),
                location.getZ(),
                location.getYaw(),
                location.getPitch());
    }

    /**
     * Converts to string.
     */
    public String toString(Location location) {
        return location.getBlockX()
                + ", "
                + location.getBlockY()
                + ", "
                + location.getBlockZ()
                + ", "
                + location.getWorld().getName();
    }
}
