package com.dreamscale.gridtime.core.machine.memory.feature.details;

import com.dreamscale.gridtime.core.machine.memory.cache.SearchKeyGenerator;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.UUID;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class Bridge implements FeatureDetails {

    private UUID projectId;

    private String boxNameA;
    private String locationPathA;

    private String boxNameB;
    private String locationPathB;

    public Bridge(UUID projectId, String fromBox, String fromLocation, String toBox, String toLocation) {
        this.projectId = projectId;
        this.boxNameA = SearchKeyGenerator.getFirstSortedBox(fromBox, fromLocation, toBox, toLocation);
        this.locationPathA = SearchKeyGenerator.getFirstSortedLocation(fromBox, fromLocation, toBox, toLocation);
        this.boxNameB = SearchKeyGenerator.getSecondSortedBox(fromBox, fromLocation, toBox, toLocation);
        this.locationPathB =  SearchKeyGenerator.getSecondSortedLocation(fromBox, fromLocation, toBox, toLocation);
    }

    @Override
    public String toSearchKey() {
        return SearchKeyGenerator.createBridgeSearchKey(projectId, boxNameA, locationPathA, boxNameB, locationPathB);
    }
}
