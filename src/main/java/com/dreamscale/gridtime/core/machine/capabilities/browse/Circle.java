package com.dreamscale.gridtime.core.machine.capabilities.browse;

import com.dreamscale.gridtime.core.machine.executor.circuit.alarm.TimeBomb;
import com.dreamscale.gridtime.core.machine.commons.DefaultCollections;

import java.util.LinkedList;

public class Circle<S> {

    private S centerOfCircle;
    LinkedList<S> similarsInCircle;
    private final CircleCapabilities circleCapabilities;


    public Circle(CircleCapabilities circleCapabilities) {
        this.similarsInCircle = DefaultCollections.queueList();
        this.circleCapabilities = circleCapabilities;
    }

    public TimeBomb refreshWithPeersAroundThisOrigin(S centerOfCircle) {
        this.centerOfCircle = centerOfCircle;
        return circleCapabilities.refreshWithPeersAroundThisOrigin(centerOfCircle);
    }

    public TimeBomb refreshWithChildrenOfThisOrigin(S centerOfCircle) {
        this.centerOfCircle = centerOfCircle;
        return circleCapabilities.refreshWithChildrenOfThisOrigin(centerOfCircle);
    }

    public TimeBomb refreshWTFsWithinThisCircle() {
        return circleCapabilities.refreshWTFsForThesePlaces(similarsInCircle);
    }

}
