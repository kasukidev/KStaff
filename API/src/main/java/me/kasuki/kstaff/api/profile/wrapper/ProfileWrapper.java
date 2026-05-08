package me.kasuki.kstaff.api.profile.wrapper;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.kasuki.kstaff.api.staff.ProfileOuterClass;

import java.util.UUID;

@RequiredArgsConstructor
@Getter
public class ProfileWrapper {
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
