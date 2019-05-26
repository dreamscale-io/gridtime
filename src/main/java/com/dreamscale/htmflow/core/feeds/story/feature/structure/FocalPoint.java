package com.dreamscale.htmflow.core.feeds.story.feature.structure;

import com.dreamscale.htmflow.core.feeds.story.mapper.SearchKeyMapper;
import com.dreamscale.htmflow.core.feeds.story.feature.FeatureFactory;
import com.dreamscale.htmflow.core.feeds.story.grid.TileGrid;

import java.time.Duration;

public class FocalPoint {

    private final Box box;

    private final GravityBallOfThoughts gravityBall;

    private LocationInBox currentLocation;

    public FocalPoint(FeatureFactory featureFactory, TileGrid tileGrid, Box box) {
        this.box = box;

        this.gravityBall = new GravityBallOfThoughts(tileGrid, featureFactory, box);
        this.currentLocation = gravityBall.getCurrentLocation();
    }

    public String getBoxName() {
        return box.getBoxName();
    }

    public void initLocation(LocationInBox lastLocationInBox) {
        gravityBall.initLocation(lastLocationInBox);
        this.currentLocation = gravityBall.getCurrentLocation();
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
        return SearchKeyMapper.createBoxKey(getBoxName());
    }


    public Box getBox() {
        return box;
    }


    public Traversal getLastTraversal() {
        return gravityBall.getLastTraversal();
    }



}
