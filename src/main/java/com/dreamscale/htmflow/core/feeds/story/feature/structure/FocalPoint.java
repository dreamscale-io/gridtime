package com.dreamscale.htmflow.core.feeds.story.feature.structure;

import com.dreamscale.htmflow.core.feeds.story.feature.IdeaFlowFeature;

import java.time.Duration;

public class FocalPoint implements IdeaFlowFeature {

    private String placeName;

    private final GravityBallOfThoughts gravityBall;
    private RadialStructure radialStructure;

    private LocationInThought currentLocation;

    private static final String ENTRANCE_OF_PLACE = "[entrance]";
    private static final String EXIT_OF_PLACE = "[exit]";


    public FocalPoint(String placeName, String initialLocationPath) {
        this.placeName = placeName;

        this.gravityBall = new GravityBallOfThoughts(this);
        this.gravityBall.gotoLocationInSpace(initialLocationPath);

        this.currentLocation = gravityBall.getCurrentLocation();

    }

    public String getName() {
        return placeName;
    }

    public LocationInThought goToLocation(String locationPath, Duration timeInLocation) {

        LocationInThought location = gravityBall.gotoLocationInSpace(locationPath);
        gravityBall.growHeavyWithFocus(timeInLocation);

        currentLocation = location;

        return currentLocation;
    }

    public void buildRadialStructure() {
        this.radialStructure = gravityBall.buildRadialStructure();
    }

    public LocationInThought getCurrentLocation() {
        return currentLocation;
    }

    public void modifyCurrentLocation(int modificationCount) {
        currentLocation.modify(modificationCount);
    }

    public LocationInThought exit() {
        return goToLocation(EXIT_OF_PLACE, Duration.ofSeconds(0));
    }

    public LocationInThought enter() {
        return goToLocation(ENTRANCE_OF_PLACE, Duration.ofSeconds(0));
    }




}
