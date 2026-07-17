package me.kasuki.kstaff.api.profile;

import me.kasuki.kstaff.api.data.ILoadable;
import me.kasuki.kstaff.api.data.IUnloadable;
import me.kasuki.kstaff.api.profile.wrapper.ProfileWrapper;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

public interface IProfileHandler extends ILoadable, IUnloadable {
    void addToCache(ProfileWrapper profileWrapper);

    void removeFromCache(UUID uniqueId);

    Optional<ProfileWrapper> getFromCache(UUID uniqueId);

    Collection<ProfileWrapper> getAllFromCache();

    void saveToDatabase(ProfileWrapper profileWrapper);

    void removeFromDatabase(UUID uniqueId);

    void saveAllToDatabase(Collection<ProfileWrapper> profileWrappers);

    void removeAllFromDatabase(Collection<UUID> uniqueIds);

    void getFromDatabase(UUID uniqueId, Consumer<Optional<ProfileWrapper>> consumer);

    void getAllFromDatabase(Consumer<Collection<ProfileWrapper>> consumer);

    /**
     * Returns whether the player with the given UUID is currently in staff mode.
     */
    default boolean isInStaffMode(UUID uniqueId) {
        return this.getFromCache(uniqueId).map(ProfileWrapper::isInStaffMode).orElse(false);
    }

    /**
     * Returns whether the player with the given UUID is currently in staff chat.
     */
    default boolean isInStaffChat(UUID uniqueId) {
        return this.getFromCache(uniqueId).map(ProfileWrapper::isInStaffChat).orElse(false);
    }
}
