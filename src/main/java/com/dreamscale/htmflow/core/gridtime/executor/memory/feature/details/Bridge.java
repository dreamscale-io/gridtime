package com.dreamscale.htmflow.core.gridtime.executor.memory.feature.details;

import com.dreamscale.htmflow.core.gridtime.executor.memory.search.SearchKeyGenerator;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class Bridge implements FeatureDetails {

    private String boxNameA;
    private String locationPathA;

    private String boxNameB;
    private String locationPathB;

    public Bridge(String fromBox, String fromLocation, String toBox, String toLocation) {
        this.boxNameA = SearchKeyGenerator.getFirstSortedBox(fromBox, fromLocation, toBox, toLocation);
        this.locationPathA = SearchKeyGenerator.getFirstSortedLocation(fromBox, fromLocation, toBox, toLocation);
        this.boxNameB = SearchKeyGenerator.getSecondSortedBox(fromBox, fromLocation, toBox, toLocation);
        this.locationPathB =  SearchKeyGenerator.getSecondSortedLocation(fromBox, fromLocation, toBox, toLocation);

    }

    @Override
    public String toSearchKey() {
        return SearchKeyGenerator.createBridgeSearchKey(boxNameA, locationPathA, boxNameB, locationPathB);
    }
}
