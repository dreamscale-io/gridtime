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

    private final String fromBox;
    private final String toBox;
    private UUID projectId;
    private String locationPathA;
    private String locationPathB;

    public Traversal(UUID projectId, String fromBox, String fromLocation, String toBox, String toLocation) {
        this.projectId = projectId;
        this.fromBox = SearchKeyGenerator.getFirstSortedBox(fromBox, fromLocation, toBox, toLocation);
        this.toBox = SearchKeyGenerator.getSecondSortedBox(fromBox, fromLocation, toBox, toLocation);
        this.locationPathA = SearchKeyGenerator.getFirstSorted(fromLocation, toLocation);
        this.locationPathB = SearchKeyGenerator.getSecondSorted(fromLocation, toLocation);
    }

    public boolean isSameBox() {
        return fromBox != null && fromBox.equals(toBox);
    }

    public String toSearchKey() {
        return SearchKeyGenerator.createTraversalSearchKey(projectId, locationPathA, locationPathB);
    }

}
