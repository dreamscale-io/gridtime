package com.dreamscale.htmflow.core.feeds.pool.feature;

import com.dreamscale.htmflow.core.feeds.pool.GridFeature;
import com.dreamscale.htmflow.core.feeds.story.mapper.SearchKeyMapper;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class Traversal implements GridFeature {

    private final String boxName;
    private String locationPathA;
    private String locationPathB;

    public Traversal(String boxName, String fromLocation, String toLocation) {
        this.boxName = boxName;
        this.locationPathA = SearchKeyMapper.getFirstSorted(fromLocation, toLocation);
        this.locationPathB = SearchKeyMapper.getSecondSorted(fromLocation, toLocation);
    }

    public String toSearchKey() {
        return SearchKeyMapper.createLocationTraversalSearchKey(boxName, locationPathA, locationPathB);
    }

}
