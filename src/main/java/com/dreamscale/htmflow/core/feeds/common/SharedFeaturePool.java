package com.dreamscale.htmflow.core.feeds.common;

import com.dreamscale.htmflow.core.feeds.clock.OuterGeometryClock;
import com.dreamscale.htmflow.core.feeds.story.StoryTile;
import com.dreamscale.htmflow.core.feeds.story.StoryTileSequence;

import java.util.HashMap;
import java.util.UUID;

public class SharedFeaturePool {

    private final UUID memberId;


    private final HashMap<ZoomLevel, StoryTileSequence> storySequenceByZoomLevel;

    private ZoomLevel activeZoomLevel;
    private OuterGeometryClock.Coords activeJobCoordinates;
    private OuterGeometryClock.Coords activeFocusCoordinates;


    public SharedFeaturePool(UUID memberId, OuterGeometryClock.Coords startingCoordinates) {

        this.memberId = memberId;

        this.storySequenceByZoomLevel = new HashMap<>();
        this.storySequenceByZoomLevel.put(ZoomLevel.MIN, new StoryTileSequence(ZoomLevel.MIN, startingCoordinates));
        this.storySequenceByZoomLevel.put(ZoomLevel.HOUR, new StoryTileSequence(ZoomLevel.HOUR, startingCoordinates));
        this.storySequenceByZoomLevel.put(ZoomLevel.DAY, new StoryTileSequence(ZoomLevel.DAY, startingCoordinates));
        this.storySequenceByZoomLevel.put(ZoomLevel.WEEK, new StoryTileSequence(ZoomLevel.WEEK, startingCoordinates));
        this.storySequenceByZoomLevel.put(ZoomLevel.BLOCK, new StoryTileSequence(ZoomLevel.BLOCK, startingCoordinates));
        this.storySequenceByZoomLevel.put(ZoomLevel.YEAR, new StoryTileSequence(ZoomLevel.YEAR, startingCoordinates));

        this.activeZoomLevel = ZoomLevel.MIN;

    }

    public StoryTile getActiveStoryFrame() {
        return storySequenceByZoomLevel.get(activeZoomLevel).getActiveStoryTile();
    }

    public void nextFrame(ZoomLevel zoomLevel) {
        StoryTileSequence storySequence = storySequenceByZoomLevel.get(zoomLevel);
        storySequence.nextFrame();
    }

    public ZoomLevel getActiveZoomLevel() {
        return this.activeZoomLevel;
    }

    //TODO this will need to load data for active coordinates, and trigger "work to do" as needed to fill in details

    public StoryTile getActiveStoryFrameAtZoomLevel(OuterGeometryClock.Coords activeFocus, ZoomLevel zoomLevel) {
        this.activeFocusCoordinates = activeFocus;
        return storySequenceByZoomLevel.get(zoomLevel).getActiveStoryTile();
    }



}
