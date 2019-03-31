package com.dreamscale.ideaflow.core.feeds.story.feature.structure;

import com.dreamscale.ideaflow.core.feeds.story.feature.IdeaFlowFeature;

import java.time.Duration;
import java.util.List;

public class FocalPoint implements IdeaFlowFeature {

    private String placeName;

    private final GravityBallOfThoughts gravityBall;

    private LocationInFocus currentLocation;
    private List<RadialStructure> extractedRadialStructures;


    public FocalPoint(String placeName, String initialLocationPath) {
        this.placeName = placeName;

        this.gravityBall = new GravityBallOfThoughts(this);
        this.gravityBall.gotoLocationInSpace(initialLocationPath);

        this.currentLocation = gravityBall.getCurrentLocation();

    }

    public String getPlaceName() {
        return placeName;
    }

    public LocationInFocus goToLocation(String locationPath, Duration timeInLocation) {

        LocationInFocus location = gravityBall.gotoLocationInSpace(locationPath);
        gravityBall.growHeavyWithFocus(timeInLocation);

        currentLocation = location;

        return currentLocation;
    }

    public List<RadialStructure> getExtractedRadialStructures() {
        return extractedRadialStructures;
    }

    public List<RadialStructure> buildRadialStructures() {
        this.extractedRadialStructures = gravityBall.buildRadialStructures();
        return this.extractedRadialStructures;
    }

    public LocationInFocus getCurrentLocation() {
        return currentLocation;
    }

    public void modifyCurrentLocation(int modificationCount) {
        currentLocation.modify(modificationCount);
    }

    public LocationInFocus exit() {
        currentLocation = gravityBall.gotoExit();

        return currentLocation;
    }

    public LocationInFocus enter() {
        currentLocation = gravityBall.gotoEntrance();

        return currentLocation;
    }




}
