package com.dreamscale.htmflow.core.gridtime.machine.memory.feature.details;

import com.dreamscale.htmflow.core.gridtime.machine.memory.cache.SearchKeyGenerator;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.UUID;

@Getter
@Setter
@ToString
public class Traversal implements FeatureDetails {

    private UUID projectId;
    private final String boxName;

    private String locationPathA;
    private String locationPathB;

    public Traversal(UUID projectId, String boxName, String fromLocation, String toLocation) {
        this.projectId = projectId;
        this.boxName = boxName;
        this.locationPathA = SearchKeyGenerator.getFirstSorted(fromLocation, toLocation);
        this.locationPathB = SearchKeyGenerator.getSecondSorted(fromLocation, toLocation);
    }

    public String toSearchKey() {
        return SearchKeyGenerator.createTraversalSearchKey(projectId, boxName, locationPathA, locationPathB);
    }

}
