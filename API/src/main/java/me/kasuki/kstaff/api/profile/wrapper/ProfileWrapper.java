package me.kasuki.kstaff.api.profile.wrapper;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.kasuki.kstaff.api.staff.ProfileOuterClass;
import me.kasuki.kstaff.utilities.item.serializer.ItemStackArraySerializer;
import me.kasuki.kstaff.utilities.item.serializer.ItemStackSerializer;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@Getter
public class ProfileWrapper {
    private final ItemStackArraySerializer serializerConstant = new ItemStackArraySerializer(new ItemStackSerializer(Bukkit.getLogger()));

    private final ProfileOuterClass.Profile profile;
    private boolean changed;

    /**
     * UUID Handling
     */
    public UUID getUniqueId() {
        try {
            return UUID.fromString(this.profile.getUuid());
        } catch (IllegalArgumentException exception) {
            exception.printStackTrace();
            return null;
        }
    }

    /**
     * Staff Mode State
     */
    public boolean isInStaffMode() {
        return this.profile.getStaffMode();
    }

    public ProfileWrapper setStaffModeState(boolean state) {
        ProfileOuterClass.Profile.Builder builder = this.profile.toBuilder();
        builder.setStaffMode(state);
        return new ProfileWrapper(builder.build());
    }

    /**
     * Staff Chat State
     */
    public boolean isInStaffChat() {
        return this.profile.getStaffChat();
    }

    public ProfileWrapper setStaffChatState(boolean state) {
        ProfileOuterClass.Profile.Builder builder = this.profile.toBuilder();
        builder.setStaffChat(state);
        return new ProfileWrapper(builder.build());
    }


    /**
     * Saved Inventory
     */
    public Optional<ItemStack[]> getSavedInventory() {
        if (!this.profile.hasSavedInventory()) {
            return Optional.empty();
        }
        return Optional.of(serializerConstant.deserialize(this.profile.getSavedInventory()));
    }

    public ProfileWrapper setSavedInventory(ItemStack[] inventory) {
        ProfileOuterClass.Profile.Builder builder = this.profile.toBuilder();
        builder.setSavedInventory(serializerConstant.serialize(inventory));
        return new ProfileWrapper(builder.build());
    }


    /**
     * Saved Armor
     */
    public Optional<ItemStack[]> getSavedArmor() {
        if (!this.profile.hasSavedArmor()) {
            return Optional.empty();
        }
        return Optional.of(serializerConstant.deserialize(this.profile.getSavedArmor()));
    }

    public ProfileWrapper setSavedArmor(ItemStack[] armor) {
        ProfileOuterClass.Profile.Builder builder = this.profile.toBuilder();
        builder.setSavedArmor(serializerConstant.serialize(armor));
        return new ProfileWrapper(builder.build());
    }


    /**
     * Data Init
     */
    public boolean hasChanged() {
        return this.changed;
    }

    public ProfileWrapper setChanged(boolean changed) {
        this.changed = changed;
        return this;
    }

    public static ProfileWrapper from(UUID uniqueId) {
        return new ProfileWrapper(
                ProfileOuterClass.Profile.newBuilder()
                        .setUuid(uniqueId.toString())
                        .setStaffChat(false)
                        .setStaffMode(false)
                        .build());
    }
}
