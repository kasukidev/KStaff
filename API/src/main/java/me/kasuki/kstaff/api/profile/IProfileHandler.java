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
}
