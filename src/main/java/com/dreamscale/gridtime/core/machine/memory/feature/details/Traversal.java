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
public class Traversal implements FeatureDetails {

    private UUID projectId;
    private String boxName;

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
