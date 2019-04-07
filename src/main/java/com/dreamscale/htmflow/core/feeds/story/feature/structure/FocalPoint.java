package com.dreamscale.htmflow.core.feeds.story.feature.structure;

import com.dreamscale.htmflow.core.feeds.executor.parts.mapper.StandardizedKeyMapper;
import com.dreamscale.htmflow.core.feeds.story.feature.FlowFeature;
import com.dreamscale.htmflow.core.feeds.executor.parts.mapper.GravityBallOfThoughts;

import java.time.Duration;
import java.util.List;

public class FocalPoint extends FlowFeature {

    private String boxName;

    private final GravityBallOfThoughts gravityBall;

    private LocationInBox currentLocation;
    private List<ThoughtBubble> extractedThoughtBubbles;


    public FocalPoint(String boxName, String initialLocationPath) {
        this.boxName = boxName;

        this.gravityBall = new GravityBallOfThoughts(this);
        this.gravityBall.gotoLocationInSpace(initialLocationPath);

        this.currentLocation = gravityBall.getCurrentLocation();

    }

    public String getBoxName() {
        return boxName;
    }

    public LocationInBox goToLocation(String locationPath, Duration timeInLocation) {

        LocationInBox location = gravityBall.gotoLocationInSpace(locationPath);
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

    public LocationInBox getCurrentLocation() {
        return currentLocation;
    }

    public void modifyCurrentLocationInFocus(int modificationCount) {
        currentLocation.modify(modificationCount);
    }

    public LocationInBox exit() {
        currentLocation = gravityBall.gotoExit();

        return currentLocation;
    }

    public LocationInBox enter() {
        currentLocation = gravityBall.gotoEntrance();

        return currentLocation;
    }

    public String toKey() {
        return StandardizedKeyMapper.createBoxKey(boxName);
    }


}
