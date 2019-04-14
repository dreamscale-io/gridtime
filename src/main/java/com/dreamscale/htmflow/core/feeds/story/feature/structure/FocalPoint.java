package com.dreamscale.htmflow.core.feeds.story.feature.structure;

import com.dreamscale.htmflow.core.feeds.executor.parts.mapper.ObjectKeyMapper;
import com.dreamscale.htmflow.core.feeds.story.feature.FeatureFactory;
import com.dreamscale.htmflow.core.feeds.story.grid.StoryGrid;

import java.time.Duration;
import java.util.List;

public class FocalPoint {

    private final Box box;

    private final GravityBallOfThoughts gravityBall;

    private LocationInBox currentLocation;

    public FocalPoint(FeatureFactory featureFactory, StoryGrid storyGrid, Box box, String initialLocationPath) {
        this.box = box;

        this.gravityBall = new GravityBallOfThoughts(storyGrid, featureFactory, box);
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

    public LocationInBox exit() {
        currentLocation = gravityBall.gotoExit();

        return currentLocation;
    }

    public LocationInBox enter() {
        currentLocation = gravityBall.gotoEntrance();

        return currentLocation;
    }

    public BoxActivity createBoxOfThoughtBubbles() {
        return gravityBall.createBoxOfThoughtBubbles();
    }


    public String toKey() {
        return ObjectKeyMapper.createBoxKey(getBoxName());
    }


    public Box getBox() {
        return box;
    }


    public Traversal getLastTraversal() {
        return gravityBall.getLastTraversal();
    }


}
