package com.dreamscale.htmflow.core.feeds.story.feature.structure;

import com.dreamscale.htmflow.core.domain.tile.FlowObjectType;
import com.dreamscale.htmflow.core.feeds.story.feature.FlowFeature;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.UUID;

@Getter
@Setter
@ToString
public class Bridge extends FlowFeature {

    private String bridgeKey;

    private LocationInBox fromLocation;
    private LocationInBox toLocation;


    public Bridge(String bridgeKey, LocationInBox fromLocation, LocationInBox toLocation) {
        this();
        this.bridgeKey = bridgeKey;
        this.fromLocation = fromLocation;
        this.toLocation = toLocation;
    }

    public Bridge() {
        super(FlowObjectType.BRIDGE);
    }

    public LocationInBox getFromLocation() {
        return fromLocation;
    }

    public LocationInBox getToLocation() {
        return toLocation;
    }

    public void setObjectId(UUID objectId) {
        setId(objectId);
    }

    public UUID getObjectId() {
        return getId();
    }

}
