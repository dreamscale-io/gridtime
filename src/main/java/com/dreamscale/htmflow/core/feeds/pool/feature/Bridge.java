package com.dreamscale.htmflow.core.feeds.pool.feature;

import com.dreamscale.htmflow.core.domain.tile.FlowObjectType;
import com.dreamscale.htmflow.core.feeds.pool.GridFeature;
import com.dreamscale.htmflow.core.feeds.story.feature.FlowFeature;
import com.dreamscale.htmflow.core.feeds.story.feature.structure.LocationInBox;
import com.dreamscale.htmflow.core.feeds.story.mapper.SearchKeyMapper;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.UUID;

@Getter
@Setter
@ToString
public class Bridge implements GridFeature {

    private String boxNameA;
    private String locationPathA;

    private String boxNameB;
    private String locationPathB;

    public Bridge(String fromBox, String fromLocation, String toBox, String toLocation) {
        this.boxNameA = SearchKeyMapper.getFirstSortedBox(fromBox, fromLocation, toBox, toLocation);
        this.locationPathA = SearchKeyMapper.getFirstSortedLocation(fromBox, fromLocation, toBox, toLocation);
        this.boxNameB = SearchKeyMapper.getSecondSortedBox(fromBox, fromLocation, toBox, toLocation);
        this.locationPathB =  SearchKeyMapper.getSecondSortedLocation(fromBox, fromLocation, toBox, toLocation);

    }

    @Override
    public String toSearchKey() {
        return SearchKeyMapper.createBridgeSearchKey(boxNameA, locationPathA, boxNameB, locationPathB);
    }
}
