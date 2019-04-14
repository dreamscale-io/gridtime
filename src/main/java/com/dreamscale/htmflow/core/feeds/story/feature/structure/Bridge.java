package com.dreamscale.htmflow.core.feeds.story.feature.structure;

import com.dreamscale.htmflow.core.feeds.story.feature.FlowFeature;
import lombok.Getter;

@Getter
public class Bridge extends FlowFeature {

    private final String bridgeKey;

    private final LocationInBox fromLocation;
    private final LocationInBox toLocation;


    public Bridge(String bridgeKey, LocationInBox fromLocation, LocationInBox toLocation) {
        this.bridgeKey = bridgeKey;
        this.fromLocation = fromLocation;
        this.toLocation = toLocation;
    }

    public LocationInBox getFromLocation() {
        return fromLocation;
    }

    public LocationInBox getToLocation() {
        return toLocation;
    }

}
