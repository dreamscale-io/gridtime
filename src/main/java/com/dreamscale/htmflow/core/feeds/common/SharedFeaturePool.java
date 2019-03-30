package com.dreamscale.htmflow.core.feeds.common;

import com.dreamscale.htmflow.core.feeds.clock.StoryGeometryClock;
import com.dreamscale.htmflow.core.feeds.story.StoryFrame;
import com.dreamscale.htmflow.core.feeds.story.StoryFrameSequence;

import java.util.HashMap;
import java.util.UUID;

public class SharedFeaturePool {

    private final UUID memberId;


    private final HashMap<ZoomLevel, StoryFrameSequence> storySequenceByZoomLevel;

    private ZoomLevel activeZoomLevel;
    private StoryGeometryClock.Coords activeJobCoordinates;
    private StoryGeometryClock.Coords activeFocusCoordinates;


    public SharedFeaturePool(UUID memberId, StoryGeometryClock.Coords startingCoordinates) {

        this.memberId = memberId;

        this.storySequenceByZoomLevel = new HashMap<>();
        this.storySequenceByZoomLevel.put(ZoomLevel.MIN, new StoryFrameSequence(ZoomLevel.MIN, startingCoordinates));
        this.storySequenceByZoomLevel.put(ZoomLevel.HOUR, new StoryFrameSequence(ZoomLevel.HOUR, startingCoordinates));
        this.storySequenceByZoomLevel.put(ZoomLevel.DAY, new StoryFrameSequence(ZoomLevel.DAY, startingCoordinates));
        this.storySequenceByZoomLevel.put(ZoomLevel.WEEK, new StoryFrameSequence(ZoomLevel.WEEK, startingCoordinates));
        this.storySequenceByZoomLevel.put(ZoomLevel.BLOCK, new StoryFrameSequence(ZoomLevel.BLOCK, startingCoordinates));
        this.storySequenceByZoomLevel.put(ZoomLevel.YEAR, new StoryFrameSequence(ZoomLevel.YEAR, startingCoordinates));

        this.activeZoomLevel = ZoomLevel.MIN;

    }

    public StoryFrame getActiveStoryFrame() {
        return storySequenceByZoomLevel.get(activeZoomLevel).getActiveStoryFrame();
    }

    public void nextFrame(ZoomLevel zoomLevel) {
        StoryFrameSequence storySequence = storySequenceByZoomLevel.get(zoomLevel);
        storySequence.nextFrame();
    }

    public ZoomLevel getActiveZoomLevel() {
        return this.activeZoomLevel;
    }

    //TODO this will need to load data for active coordinates, and trigger "work to do" as needed to fill in details

    public StoryFrame getActiveStoryFrameAtZoomLevel(StoryGeometryClock.Coords activeFocus, ZoomLevel zoomLevel) {
        this.activeFocusCoordinates = activeFocus;
        return storySequenceByZoomLevel.get(zoomLevel).getActiveStoryFrame();
    }



}
