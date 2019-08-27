package com.dreamscale.gridtime.core.machine.memory.feature.details;

import com.dreamscale.gridtime.core.machine.memory.cache.SearchKeyGenerator;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class LocationInBox implements FeatureDetails {

    private final UUID projectId;
    private final String boxName;
    private final String locationPath;

    public LocationInBox(UUID projectId, String boxName, String locationPath) {
        this.projectId = projectId;
        this.boxName = boxName;
        this.locationPath = locationPath;
    }

    public String toSearchKey() {
       return SearchKeyGenerator.createLocationSearchKey(projectId, locationPath);
    }


    public String toString() {
        return toSearchKey();
    }

}
