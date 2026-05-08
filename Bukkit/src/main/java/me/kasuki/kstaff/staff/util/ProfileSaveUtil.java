package me.kasuki.kstaff.staff.util;

import java.util.ArrayList;
import java.util.List;
import lombok.experimental.UtilityClass;
import me.kasuki.kstaff.api.profile.IProfileHandler;
import me.kasuki.kstaff.api.profile.wrapper.ProfileWrapper;

@UtilityClass
public class ProfileSaveUtil {
    public final int BATCH_SIZE = 100;

    public void processProfileSaving(IProfileHandler profileHandler) {
        List<ProfileWrapper> profileWrappers = new ArrayList<>(profileHandler.getAllFromCache());
        profileWrappers.removeIf(profileWrapper -> !profileWrapper.hasChanged());

        if (profileWrappers.isEmpty()) {
            return;
        }

        int size = profileWrappers.size();

        if (size <= BATCH_SIZE) {
            processBatch(profileHandler, profileWrappers);
            return;
        }

        for (int index = 0; index < size; index += BATCH_SIZE) {
            int end = Math.min(index + BATCH_SIZE, size);
            List<ProfileWrapper> batchList = profileWrappers.subList(index, end);
            processBatch(profileHandler, batchList);
        }
    }

    private void processBatch(IProfileHandler profileHandler, List<ProfileWrapper> profileWrappers) {
        profileHandler.saveAllToDatabase(profileWrappers);
        profileWrappers.forEach(
                profileWrapper -> {
                    profileWrapper = profileWrapper.setChanged(false);
                    profileHandler.addToCache(profileWrapper);
                });
    }
}
