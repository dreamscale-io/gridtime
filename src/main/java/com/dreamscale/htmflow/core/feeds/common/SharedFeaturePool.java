package com.dreamscale.htmflow.core.feeds.common;

import com.dreamscale.htmflow.core.feeds.clock.GeometryClock;
import com.dreamscale.htmflow.core.feeds.clock.ZoomLevel;
import com.dreamscale.htmflow.core.feeds.executor.parts.mapper.ObjectKeyMapper;
import com.dreamscale.htmflow.core.feeds.executor.parts.mapper.URIMapper;
import com.dreamscale.htmflow.core.feeds.story.StoryTile;
import com.dreamscale.htmflow.core.feeds.story.StoryTileSequence;

import java.util.HashMap;
import java.util.UUID;

public class SharedFeaturePool {

    private final String feedUri;


    private final HashMap<ZoomLevel, StoryTileSequence> storySequenceByZoomLevel;

    private ZoomLevel activeZoomLevel;
    private GeometryClock.Coords activeJobCoordinates;
    private GeometryClock.Coords activeFocusCoordinates;


    public SharedFeaturePool(UUID torchieId, GeometryClock.Coords startingCoordinates) {

        this.feedUri = URIMapper.createTorchieFeedUri(torchieId);

        this.storySequenceByZoomLevel = new HashMap<>();
        this.storySequenceByZoomLevel.put(ZoomLevel.MIN, new StoryTileSequence(feedUri, ZoomLevel.MIN, startingCoordinates));
        this.storySequenceByZoomLevel.put(ZoomLevel.HOUR, new StoryTileSequence(feedUri, ZoomLevel.HOUR, startingCoordinates));
        this.storySequenceByZoomLevel.put(ZoomLevel.DAY, new StoryTileSequence(feedUri, ZoomLevel.DAY, startingCoordinates));
        this.storySequenceByZoomLevel.put(ZoomLevel.WEEK, new StoryTileSequence(feedUri, ZoomLevel.WEEK, startingCoordinates));
        this.storySequenceByZoomLevel.put(ZoomLevel.BLOCK, new StoryTileSequence(feedUri, ZoomLevel.BLOCK, startingCoordinates));
        this.storySequenceByZoomLevel.put(ZoomLevel.YEAR, new StoryTileSequence(feedUri, ZoomLevel.YEAR, startingCoordinates));

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

    public StoryTile getActiveStoryFrameAtZoomLevel(GeometryClock.Coords activeFocus, ZoomLevel zoomLevel) {
        this.activeFocusCoordinates = activeFocus;
        return storySequenceByZoomLevel.get(zoomLevel).getActiveStoryTile();
    }



}
