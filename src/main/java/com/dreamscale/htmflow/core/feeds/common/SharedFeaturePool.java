package com.dreamscale.htmflow.core.feeds.common;

import com.dreamscale.htmflow.core.feeds.clock.OuterGeometryClock;
import com.dreamscale.htmflow.core.feeds.executor.parts.mapper.StandardizedKeyMapper;
import com.dreamscale.htmflow.core.feeds.story.StoryFrame;
import com.dreamscale.htmflow.core.feeds.story.StoryFrameSequence;

import java.util.HashMap;
import java.util.UUID;

public class SharedFeaturePool {

    private final String feedUri;


    private final HashMap<ZoomLevel, StoryFrameSequence> storySequenceByZoomLevel;

    private ZoomLevel activeZoomLevel;
    private OuterGeometryClock.Coords activeJobCoordinates;
    private OuterGeometryClock.Coords activeFocusCoordinates;


    public SharedFeaturePool(UUID torchieId, OuterGeometryClock.Coords startingCoordinates) {

        this.feedUri = StandardizedKeyMapper.createTorchieFeedUri(torchieId);

        this.storySequenceByZoomLevel = new HashMap<>();
        this.storySequenceByZoomLevel.put(ZoomLevel.MIN, new StoryFrameSequence(feedUri, ZoomLevel.MIN, startingCoordinates));
        this.storySequenceByZoomLevel.put(ZoomLevel.HOUR, new StoryFrameSequence(feedUri, ZoomLevel.HOUR, startingCoordinates));
        this.storySequenceByZoomLevel.put(ZoomLevel.DAY, new StoryFrameSequence(feedUri, ZoomLevel.DAY, startingCoordinates));
        this.storySequenceByZoomLevel.put(ZoomLevel.WEEK, new StoryFrameSequence(feedUri, ZoomLevel.WEEK, startingCoordinates));
        this.storySequenceByZoomLevel.put(ZoomLevel.BLOCK, new StoryFrameSequence(feedUri, ZoomLevel.BLOCK, startingCoordinates));
        this.storySequenceByZoomLevel.put(ZoomLevel.YEAR, new StoryFrameSequence(feedUri, ZoomLevel.YEAR, startingCoordinates));

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

    public StoryFrame getActiveStoryFrameAtZoomLevel(OuterGeometryClock.Coords activeFocus, ZoomLevel zoomLevel) {
        this.activeFocusCoordinates = activeFocus;
        return storySequenceByZoomLevel.get(zoomLevel).getActiveStoryFrame();
    }



}
