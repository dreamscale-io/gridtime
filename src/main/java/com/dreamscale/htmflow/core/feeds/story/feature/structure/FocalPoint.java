package com.dreamscale.htmflow.core.feeds.story.feature.structure;

import com.dreamscale.htmflow.core.feeds.story.feature.FlowFeature;
import com.dreamscale.htmflow.core.feeds.executor.parts.mapper.GravityBallOfThoughts;

import java.time.Duration;
import java.util.List;

public class FocalPoint implements FlowFeature {

    private String placeName;

    private final GravityBallOfThoughts gravityBall;

    private LocationInFocus currentLocation;
    private List<ThoughtBubble> extractedThoughtBubbles;


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

    public List<ThoughtBubble> getThoughtBubbles() {
        if (extractedThoughtBubbles == null) {
            this.extractedThoughtBubbles = gravityBall.extractThoughtBubbles();
        }
        return extractedThoughtBubbles;
    }

    private List<ThoughtBubble> extractThoughtBubbles() {
        this.extractedThoughtBubbles = gravityBall.extractThoughtBubbles();
        return this.extractedThoughtBubbles;
    }

    public LocationInFocus getCurrentLocation() {
        return currentLocation;
    }

    public void modifyCurrentLocationInFocus(int modificationCount) {
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
