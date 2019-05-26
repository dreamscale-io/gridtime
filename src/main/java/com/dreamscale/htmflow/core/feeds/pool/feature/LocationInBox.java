package com.dreamscale.htmflow.core.feeds.pool.feature;

import com.dreamscale.htmflow.core.feeds.pool.GridFeature;
import com.dreamscale.htmflow.core.feeds.story.mapper.SearchKeyMapper;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LocationInBox implements GridFeature {

    private final String boxName;
    private final String locationPath;

    public LocationInBox(String boxName, String locationPath) {
        this.boxName = boxName;
        this.locationPath = locationPath;
    }

    public String toSearchKey() {
       return SearchKeyMapper.createLocationSearchKey(boxName, locationPath);
    }

    public String toString() {
        return toSearchKey();
    }

}
