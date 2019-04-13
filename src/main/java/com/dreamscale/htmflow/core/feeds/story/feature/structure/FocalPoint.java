package com.dreamscale.htmflow.core.feeds.story.feature.structure;

import com.dreamscale.htmflow.core.feeds.executor.parts.mapper.StandardizedKeyMapper;
import com.dreamscale.htmflow.core.feeds.story.feature.FlowFeature;

import java.time.Duration;

public class FocalPoint {

    private final Box box;

    private final GravityBallOfThoughts gravityBall;

    private LocationInBox currentLocation;

    public FocalPoint(String boxName, String initialLocationPath) {
        this.box = new Box(boxName);

        this.gravityBall = new GravityBallOfThoughts(this);
        this.gravityBall.initStartingLocation(initialLocationPath);

        this.currentLocation = gravityBall.getCurrentLocation();
    }

    public String getBoxName() {
        return box.getBoxName();
    }

    public LocationInBox goToLocation(String locationPath, Duration timeInLocation) {

        LocationInBox location = gravityBall.gotoLocationInSpace(locationPath);
        gravityBall.growHeavyWithFocus(timeInLocation);

        currentLocation = location;

        return currentLocation;
    }

    public LocationInBox getCurrentLocation() {
        return currentLocation;
    }

    public void modifyCurrentLocationInFocus(int modificationCount) {
        box.modify(modificationCount);
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

    public void loadThoughtsIntoBox() {
        box.setThoughtBubbles(gravityBall.extractThoughtBubbles());
    }

    public String toKey() {
        return StandardizedKeyMapper.createBoxKey(getBoxName());
    }


    public Box getBox() {
        return box;
    }


    public Traversal getLastTraversal() {
        return gravityBall.getLastTraversal();
    }
}
