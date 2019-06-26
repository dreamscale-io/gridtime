package com.dreamscale.htmflow.core.gridtime.capabilities.browse;

import com.dreamscale.htmflow.core.gridtime.kernel.executor.circuit.alarm.TimeBomb;

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

    public TimeBomb refreshAsPeerCircle() {
        TimeBomb fireWhenDone = circleOfOneHopAwayFromThisPlace.refreshWithPeersAroundThisOrigin(this);

        return fireWhenDone;
    }

    public TimeBomb refreshAsChildrenCircle() {
        TimeBomb fireWhenDone = circleOfOneHopAwayFromThisPlace.refreshWithChildrenOfThisOrigin(this);

        return fireWhenDone;
    }

    public TimeBomb refreshWTFs() {
        TimeBomb fireWhenDone = capabilities.refreshWTFsWithinThisPlace(this);

        return fireWhenDone;
    }

    public TimeBomb refreshCircleWTFs() {
        TimeBomb fireWhenDone = circleOfOneHopAwayFromThisPlace.refreshWTFsWithinThisCircle();

        return fireWhenDone;
    }

}
