package com.dreamscale.htmflow.core.gridtime.executor.machine.capabilities.see;

import com.dreamscale.htmflow.core.gridtime.executor.alarm.TimeBombTrigger;
import com.dreamscale.htmflow.core.gridtime.executor.machine.parts.commons.DefaultCollections;

import java.util.LinkedList;

public class Circle<S> {

    private S centerOfCircle;
    LinkedList<S> similarsInCircle;
    private final CircleCapabilities circleCapabilities;


    public Circle(CircleCapabilities circleCapabilities) {
        this.similarsInCircle = DefaultCollections.queueList();
        this.circleCapabilities = circleCapabilities;
    }

    public TimeBombTrigger refreshWithPeersAroundThisOrigin(S centerOfCircle) {
        this.centerOfCircle = centerOfCircle;
        return circleCapabilities.refreshWithPeersAroundThisOrigin(centerOfCircle);
    }

    public TimeBombTrigger refreshWithChildrenOfThisOrigin(S centerOfCircle) {
        this.centerOfCircle = centerOfCircle;
        return circleCapabilities.refreshWithChildrenOfThisOrigin(centerOfCircle);
    }

    public TimeBombTrigger refreshWTFsWithinThisCircle() {
        return circleCapabilities.refreshWTFsForThesePlaces(similarsInCircle);
    }

}
