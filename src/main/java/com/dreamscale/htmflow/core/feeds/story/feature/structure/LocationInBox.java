package com.dreamscale.htmflow.core.feeds.story.feature.structure;

import com.dreamscale.htmflow.core.feeds.executor.parts.mapper.StandardizedKeyMapper;
import com.dreamscale.htmflow.core.feeds.story.feature.FlowFeature;

import java.time.Duration;

public class LocationInBox extends FlowFeature {

    private final FocalPoint box;
    private final String locationPath;
    private final int locationIndex;

    private int modificationCount;
    private Duration totalTimeInvestment;
    private int visitCounter;

    public LocationInBox(FocalPoint box, String locationPath, int locationIndex) {
        this.box = box;
        this.locationPath = locationPath;
        this.totalTimeInvestment = Duration.ofSeconds(0);
        this.visitCounter = 0;
        this.modificationCount = 0;
        this.locationIndex = locationIndex;
    }

    public String getLocationPath() {
        return locationPath;
    }

    public void visit() {
        this.visitCounter++;
    }

    public void spendTime(Duration additionalTime) {
        this.totalTimeInvestment = totalTimeInvestment.plus(additionalTime);
    }

    public void modify(int modificationCount) {
        this.modificationCount += modificationCount;
    }

    public String getBoxName() {
        return box.getBoxName();
    }

    public String toKey() {
       return box.toKey() + ":" + StandardizedKeyMapper.createLocationKey(locationPath);
    }

    public Duration getTotalTimeInvestment() {
        return totalTimeInvestment;
    }

    public String toString() {
        return toKey();
    }
}
