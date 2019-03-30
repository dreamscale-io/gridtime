package com.dreamscale.htmflow.core.feeds.story.feature.structure;

import com.dreamscale.htmflow.core.feeds.story.feature.FlowFeature;

import java.time.Duration;

public class FocalPoint implements FlowFeature {

    private String placeName;

    private final GravityBall gravityBall;

    private LocationInPlace currentLocation;

    private static final String ENTRANCE_OF_PLACE = "[entrance]";
    private static final String EXIT_OF_PLACE = "[exit]";


    public FocalPoint(String placeName, String initialLocationPath) {
        this.placeName = placeName;

        this.gravityBall = new GravityBall(this);
        this.gravityBall.gotoLocationInSpace(initialLocationPath);

        this.currentLocation = gravityBall.getCurrentLocation();

    }

    public String getName() {
        return placeName;
    }

    public LocationInPlace goToLocation(String locationPath, Duration timeInLocation) {

        LocationInPlace location = gravityBall.gotoLocationInSpace(locationPath);
        gravityBall.growHeavyWithFocus(timeInLocation);

        currentLocation = location;

        return currentLocation;
    }


    public LocationInPlace getCurrentLocation() {
        return currentLocation;
    }

    public void modifyCurrentLocation(int modificationCount) {
        currentLocation.modify(modificationCount);
    }

    public LocationInPlace exit() {
        return goToLocation(EXIT_OF_PLACE, Duration.ofSeconds(0));
    }

    public LocationInPlace enter() {
        return goToLocation(ENTRANCE_OF_PLACE, Duration.ofSeconds(0));
    }




}
