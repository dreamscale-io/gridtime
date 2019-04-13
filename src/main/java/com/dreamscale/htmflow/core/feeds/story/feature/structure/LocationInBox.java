package com.dreamscale.htmflow.core.feeds.story.feature.structure;

import com.dreamscale.htmflow.core.feeds.executor.parts.mapper.StandardizedKeyMapper;
import com.dreamscale.htmflow.core.feeds.story.feature.FlowFeature;
import com.dreamscale.htmflow.core.feeds.story.feature.metrics.GridObject;
import com.dreamscale.htmflow.core.feeds.story.feature.metrics.GridObjectMetrics;

import java.time.Duration;

public class LocationInBox extends FlowFeature implements GridObject {

    private final FocalPoint focalPoint;
    private final String locationPath;

    private GridObjectMetrics gridObjectMetrics = new GridObjectMetrics();


    public LocationInBox(FocalPoint focalPoint, String locationPath) {
        this.focalPoint = focalPoint;
        this.locationPath = locationPath;
    }

    public String getLocationPath() {
        return locationPath;
    }


    public void spendTime(Duration additionalTime) {
        gridObjectMetrics.addVelocitySample(additionalTime.getSeconds());
    }

    public void modify(int modificationCount) {
        gridObjectMetrics.addModificationSample(modificationCount);
    }

    public String getBoxName() {
        return focalPoint.getBoxName();
    }

    public String toKey() {
       return focalPoint.toKey() + ":" + StandardizedKeyMapper.createLocationKey(locationPath);
    }

    public Duration getTotalTimeInvestment() {
        return gridObjectMetrics.getTotalTimeInvestment();
    }

    public String toString() {
        return toKey();
    }

    @Override
    public GridObjectMetrics getGridObjectMetrics() {
        return gridObjectMetrics;
    }
}
