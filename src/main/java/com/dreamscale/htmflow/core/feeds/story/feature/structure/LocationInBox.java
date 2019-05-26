package com.dreamscale.htmflow.core.feeds.story.feature.structure;

import com.dreamscale.htmflow.core.domain.tile.FlowObjectType;
import com.dreamscale.htmflow.core.feeds.story.feature.FlowFeature;
import com.dreamscale.htmflow.core.feeds.story.mapper.SearchKeyMapper;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class LocationInBox extends FlowFeature {

    private String boxName;
    private String locationPath;

    public LocationInBox(String boxName, String locationPath) {
        this();
        this.boxName = boxName;
        this.locationPath = locationPath;
    }

    public LocationInBox() {
        super(FlowObjectType.LOCATION);
    }

    public void setObjectId(UUID objectId) {
        setId(objectId);
    }

    public UUID getObjectId() {
        return getId();
    }

    public String toKey() {
       return SearchKeyMapper.createLocationSearchKey(boxName, locationPath);
    }

    public String toString() {
        return toKey();
    }

}
