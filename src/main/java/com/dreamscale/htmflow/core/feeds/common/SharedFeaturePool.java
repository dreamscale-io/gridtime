package com.dreamscale.htmflow.core.feeds.common;

import com.dreamscale.htmflow.core.feeds.clock.GeometryClock;
import com.dreamscale.htmflow.core.feeds.clock.ZoomLevel;
import com.dreamscale.htmflow.core.feeds.executor.parts.mapper.URIMapper;
import com.dreamscale.htmflow.core.feeds.story.StoryTile;
import com.dreamscale.htmflow.core.feeds.story.StoryTileSequence;

import java.util.HashMap;
import java.util.UUID;

public class SharedFeaturePool {

    private final String feedUri;


    private final HashMap<ZoomLevel, StoryTileSequence> storySequenceByZoomLevel;

    private ZoomLevel activeZoomLevel;
    private GeometryClock.StoryCoords activeJobCoordinates;
    private GeometryClock.StoryCoords activeFocusCoordinates;


    public SharedFeaturePool(UUID torchieId, GeometryClock.StoryCoords startingCoordinates) {

        this.feedUri = URIMapper.createTorchieFeedUri(torchieId);

        this.storySequenceByZoomLevel = new HashMap<>();
        this.storySequenceByZoomLevel.put(ZoomLevel.TWENTY_MINS, new StoryTileSequence(feedUri, ZoomLevel.TWENTY_MINS, startingCoordinates));
        this.storySequenceByZoomLevel.put(ZoomLevel.FOUR_HOURS, new StoryTileSequence(feedUri, ZoomLevel.FOUR_HOURS, startingCoordinates));
        this.storySequenceByZoomLevel.put(ZoomLevel.DAYS, new StoryTileSequence(feedUri, ZoomLevel.DAYS, startingCoordinates));
        this.storySequenceByZoomLevel.put(ZoomLevel.WEEKS, new StoryTileSequence(feedUri, ZoomLevel.WEEKS, startingCoordinates));
        this.storySequenceByZoomLevel.put(ZoomLevel.BLOCKS, new StoryTileSequence(feedUri, ZoomLevel.BLOCKS, startingCoordinates));
        this.storySequenceByZoomLevel.put(ZoomLevel.YEAR, new StoryTileSequence(feedUri, ZoomLevel.YEAR, startingCoordinates));

        this.activeZoomLevel = ZoomLevel.TWENTY_MINS;

    }

    public StoryTile getActiveStoryTile() {
        return storySequenceByZoomLevel.get(activeZoomLevel).getActiveStoryTile();
    }

    public void nextTile(ZoomLevel zoomLevel) {
        StoryTileSequence storySequence = storySequenceByZoomLevel.get(zoomLevel);
        storySequence.nextFrame();
    }

    public ZoomLevel getActiveZoomLevel() {
        return this.activeZoomLevel;
    }

    //TODO this will need to load data for active coordinates, and trigger "work to do" as needed to fill in details

    public StoryTile getActiveStoryTileAtZoomLevel(GeometryClock.StoryCoords activeFocus, ZoomLevel zoomLevel) {
        this.activeFocusCoordinates = activeFocus;
        return storySequenceByZoomLevel.get(zoomLevel).getActiveStoryTile();
    }



}
