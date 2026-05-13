package me.kasuki.kstaff.data.profile;

import com.google.protobuf.InvalidProtocolBufferException;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import me.kasuki.kstaff.KStaffPlugin;
import me.kasuki.kstaff.api.profile.IProfileHandler;
import me.kasuki.kstaff.api.profile.repository.SQLiteProfileRepository;
import me.kasuki.kstaff.api.profile.wrapper.ProfileWrapper;
import me.kasuki.kstaff.api.staff.ProfileOuterClass;
import me.kasuki.kstaff.data.profile.cache.ProfileCacheHandler;

public class SQLiteProfileHandler implements IProfileHandler {

    private final ProfileCacheHandler profileCacheHandler;
    private final SQLiteProfileRepository sqLiteProfileRepository;

    public SQLiteProfileHandler(KStaffPlugin instance) {
        this.profileCacheHandler = new ProfileCacheHandler();
        this.sqLiteProfileRepository = new SQLiteProfileRepository(instance);
    }

    @Override
    public void addToCache(ProfileWrapper profileWrapper) {
        this.profileCacheHandler.addToCache(profileWrapper);
    }

    @Override
    public void removeFromCache(UUID uniqueId) {
        this.profileCacheHandler.removeFromCache(uniqueId);
    }

    @Override
    public Optional<ProfileWrapper> getFromCache(UUID uniqueId) {
        return this.profileCacheHandler.getFromCache(uniqueId);
    }

    @Override
    public Collection<ProfileWrapper> getAllFromCache() {
        return this.profileCacheHandler.getAllFromCache();
    }

    @Override
    public void saveToDatabase(ProfileWrapper profileWrapper) {
        this.sqLiteProfileRepository.saveToDatabase(
                profileWrapper.getUniqueId(), profileWrapper.getProfile().toByteArray());
    }

    @Override
    public void removeFromDatabase(UUID uniqueId) {
        this.sqLiteProfileRepository.removeFromDatabase(uniqueId);
    }

    @Override
    public void saveAllToDatabase(Collection<ProfileWrapper> profileWrappers) {
        Map<UUID, byte[]> map = new HashMap<>();
        profileWrappers.forEach(
                profileWrapper ->
                        map.put(profileWrapper.getUniqueId(), profileWrapper.getProfile().toByteArray()));
        this.sqLiteProfileRepository.saveBatch(map);
    }

    @Override
    public void removeAllFromDatabase(Collection<UUID> uniqueIds) {
        this.sqLiteProfileRepository.deleteBatch(uniqueIds);
    }

    private Optional<ProfileWrapper> processBytes(byte[] input) {
        try {
            ProfileOuterClass.Profile profile = ProfileOuterClass.Profile.parseFrom(input);
            return Optional.of(new ProfileWrapper(profile));
        } catch (InvalidProtocolBufferException exception) {
            exception.printStackTrace();
            return Optional.empty();
        }
    }

    @Override
    public void getFromDatabase(UUID uniqueId, Consumer<Optional<ProfileWrapper>> consumer) {
        this.sqLiteProfileRepository.getFromDatabase(
                uniqueId,
                optional -> {
                    if (!optional.isPresent()) {
                        consumer.accept(Optional.empty());
                        return;
                    }

                    consumer.accept(this.processBytes(optional.get()));
                });
    }

    @Override
    public void getAllFromDatabase(Consumer<Collection<ProfileWrapper>> consumer) {
        this.sqLiteProfileRepository.getAllEntriesFromDatabase(
                list ->
                        consumer.accept(
                                list.stream()
                                        .map(this::processBytes)
                                        .filter(Optional::isPresent)
                                        .map(Optional::get)
                                        .collect(Collectors.toList())));
    }

    @Override
    public void load() {
        this.getAllFromDatabase(list -> list.forEach(this::addToCache));
    }

    @Override
    public void unload() {
        this.saveAllToDatabase(this.profileCacheHandler.getAllFromCache());
    }
}
