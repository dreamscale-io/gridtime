package com.dreamscale.htmflow.core.feeds.executor.parts.pool;

import com.dreamscale.htmflow.core.feeds.clock.ZoomLevel;
import com.dreamscale.htmflow.core.feeds.clock.GeometryClock;
import com.dreamscale.htmflow.core.feeds.story.StoryTile;

import java.util.LinkedList;

public class StoryTileSequence {

    private final String feedUri;
    private final ZoomLevel zoomLevel;
    private GeometryClock.Coords activeStoryCoordinates;

    private StoryTile activeStoryTile;

    public StoryTileSequence(String feedUri, ZoomLevel zoomLevel, GeometryClock.Coords storyCoordinates) {
        this.feedUri = feedUri;
        this.zoomLevel = zoomLevel;
        this.activeStoryCoordinates = storyCoordinates;
        this.activeStoryTile = new StoryTile(feedUri, storyCoordinates, zoomLevel);

    }

    public void nextFrame() {
        this.activeStoryCoordinates = activeStoryCoordinates.panRight(zoomLevel);

        StoryTile nextFrame = new StoryTile(feedUri, activeStoryCoordinates, zoomLevel);

        nextFrame.carryOverFrameContext(this.activeStoryTile);
        this.activeStoryTile = nextFrame;

    }

    public StoryTile getActiveStoryTile() {
        return this.activeStoryTile;
    }
}
