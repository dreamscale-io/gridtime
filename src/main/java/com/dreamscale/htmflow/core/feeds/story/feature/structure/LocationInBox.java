package com.dreamscale.htmflow.core.feeds.story.feature.structure;

import com.dreamscale.htmflow.core.domain.tile.FlowObjectType;
import com.dreamscale.htmflow.core.feeds.executor.parts.mapper.ObjectKeyMapper;
import com.dreamscale.htmflow.core.feeds.story.feature.FlowFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.UUID;

@Getter
@Setter
public class LocationInBox extends FlowFeature  {

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
       return ObjectKeyMapper.createBoxLocationKey(boxName, locationPath);
    }

    public String toString() {
        return toKey();
    }

}
