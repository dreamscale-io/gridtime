package com.dreamscale.htmflow.core.feeds.story.feature.structure;

import com.dreamscale.htmflow.core.feeds.story.feature.FlowFeature;

import java.time.Duration;

public class LocationInPlace implements FlowFeature {

    private final FocalPoint place;
    private final String locationPath;
    private final int locationIndex;

    private int modificationCount;
    private Duration timeInPlace;
    private int visitCounter;

    LocationInPlace(FocalPoint place, String locationPath, int locationIndex) {
        this.place = place;
        this.locationPath = locationPath;
        this.timeInPlace = Duration.ofSeconds(0);
        this.visitCounter = 0;
        this.modificationCount = 0;
        this.locationIndex = locationIndex;
    }

    public String getLocationPath() {
        return locationPath;
    }

    void visit() {
        this.visitCounter++;
    }

    public void spendTime(Duration additionalTime) {
        this.timeInPlace = timeInPlace.plus(additionalTime);
    }

    public void modify(int modificationCount) {
        this.modificationCount += modificationCount;
    }

    public String getPlaceName() {
        return place.getName();
    }

    public String toKey() {
       return "["+ place.getName() + "]:"+locationPath;
    }
}
