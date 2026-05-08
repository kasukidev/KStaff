package me.kasuki.kstaff.staff.cache;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import me.kasuki.kstaff.api.profile.wrapper.ProfileWrapper;

public class ProfileCacheHandler {

  private final Map<UUID, ProfileWrapper> cache = new ConcurrentHashMap<>();

  public void addToCache(ProfileWrapper profileWrapper) {
    this.cache.put(profileWrapper.getUniqueId(), profileWrapper);
  }

  public void removeFromCache(UUID uuid) {
    this.cache.remove(uuid);
  }

  public Optional<ProfileWrapper> getFromCache(UUID uuid) {
    return Optional.ofNullable(this.cache.get(uuid));
  }

  public Collection<ProfileWrapper> getAllFromCache() {
    return this.cache.values();
  }
}
