package com.dreamscale.htmflow.core.feeds.story.feature.structure;

import com.dreamscale.htmflow.core.feeds.executor.parts.mapper.ObjectKeyMapper;
import com.dreamscale.htmflow.core.feeds.story.feature.FlowFeature;

public class LocationInBox extends FlowFeature  {

    private final String boxName;
    private final String locationPath;


    public LocationInBox(String boxName, String locationPath) {
        this.boxName = boxName;
        this.locationPath = locationPath;
    }

    public String getLocationPath() {
        return locationPath;
    }

    public String getBoxName() {
        return boxName;
    }

    public String toKey() {
       return boxName + ":" + ObjectKeyMapper.createLocationKey(locationPath);
    }

    public String toString() {
        return toKey();
    }

}
