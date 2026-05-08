package me.kasuki.kstaff.api.module;

import java.util.Collection;
import java.util.Collections;

public interface IModule {
    String getId();

    default Collection<String> getDependencies() {
        return Collections.emptyList();
    }
}
