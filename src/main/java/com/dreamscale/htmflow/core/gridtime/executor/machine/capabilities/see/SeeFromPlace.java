package com.dreamscale.htmflow.core.gridtime.executor.machine.capabilities.see;

import com.dreamscale.htmflow.core.gridtime.executor.alarm.TimeBombTrigger;

import java.util.concurrent.atomic.AtomicInteger;

public class SeeFromPlace {

    private TracerArrow<SeeFromPlace> tracerOfPlacesWhereIveBeen;
    private Circle<SeeFromPlace> circleOfOneHopAwayFromThisPlace;

    private SeeFromPlaceCapabilities capabilities;

    private AtomicInteger visitCount;

    public SeeFromPlace(SeeFromPlaceCapabilities capabilities) {
        this.tracerOfPlacesWhereIveBeen = new TracerArrow<>(capabilities.getTracerArrowCapabilities());
        this.circleOfOneHopAwayFromThisPlace = new Circle<>(capabilities.getCircleCapabilities());
        this.visitCount = new AtomicInteger(0);

        this.capabilities = capabilities;
    }

    public SeeFromPlace(TracerArrow<SeeFromPlace> tracerOfPlacesWhereIveBeen, Circle<SeeFromPlace> circleOfOneHopAwayFromThisPlace) {
        this.tracerOfPlacesWhereIveBeen = tracerOfPlacesWhereIveBeen;
        this.circleOfOneHopAwayFromThisPlace = circleOfOneHopAwayFromThisPlace;
    }

    public void visit() {
        this.visitCount.getAndIncrement();
    }

    public SeeFromPlace gotoPlaceConnectedFromHere(SeeFromPlace gotoPlace) {
        tracerOfPlacesWhereIveBeen.push(gotoPlace);
        return gotoPlace;
    }

    public TimeBombTrigger refreshAsPeerCircle() {
        TimeBombTrigger fireWhenDone = circleOfOneHopAwayFromThisPlace.refreshWithPeersAroundThisOrigin(this);

        return fireWhenDone;
    }

    public TimeBombTrigger refreshAsChildrenCircle() {
        TimeBombTrigger fireWhenDone = circleOfOneHopAwayFromThisPlace.refreshWithChildrenOfThisOrigin(this);

        return fireWhenDone;
    }

    public TimeBombTrigger refreshWTFs() {
        TimeBombTrigger fireWhenDone = capabilities.refreshWTFsWithinThisPlace(this);

        return fireWhenDone;
    }

    public TimeBombTrigger refreshCircleWTFs() {
        TimeBombTrigger fireWhenDone = circleOfOneHopAwayFromThisPlace.refreshWTFsWithinThisCircle();

        return fireWhenDone;
    }

}
