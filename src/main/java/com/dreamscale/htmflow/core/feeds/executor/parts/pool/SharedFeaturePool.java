package com.dreamscale.htmflow.core.feeds.executor.parts.pool;

import com.dreamscale.htmflow.core.feeds.clock.GeometryClock;
import com.dreamscale.htmflow.core.feeds.clock.ZoomLevel;
import com.dreamscale.htmflow.core.feeds.executor.parts.fetch.TileLoader;
import com.dreamscale.htmflow.core.feeds.executor.parts.mapper.URIMapper;
import com.dreamscale.htmflow.core.feeds.story.StoryTile;
import com.dreamscale.htmflow.core.feeds.story.feature.CarryOverContext;

import java.util.HashMap;
import java.util.UUID;

public class SharedFeaturePool {

    private final String feedUri;


    private final HashMap<ZoomLevel, StoryTileSequence> storySequenceByZoomLevel;
    private final TileLoader tileLoader;
    private final UUID torchieId;

    private ZoomLevel activeZoomLevel;
    private GeometryClock.Coords activeJobCoordinates;
    private GeometryClock.Coords activeFocusCoordinates;


    public SharedFeaturePool(UUID torchieId, GeometryClock.Coords startingCoordinates, TileLoader tileLoader) {

        this.torchieId = torchieId;
        this.feedUri = URIMapper.createTorchieFeedUri(torchieId);

        this.tileLoader = tileLoader;

        this.storySequenceByZoomLevel = new HashMap<>();
        this.storySequenceByZoomLevel.put(ZoomLevel.TWENTY_MINS, new StoryTileSequence(ZoomLevel.TWENTY_MINS, startingCoordinates));
        this.storySequenceByZoomLevel.put(ZoomLevel.FOUR_HOURS, new StoryTileSequence(ZoomLevel.FOUR_HOURS, startingCoordinates));
        this.storySequenceByZoomLevel.put(ZoomLevel.DAYS, new StoryTileSequence(ZoomLevel.DAYS, startingCoordinates));
        this.storySequenceByZoomLevel.put(ZoomLevel.WEEKS, new StoryTileSequence(ZoomLevel.WEEKS, startingCoordinates));
        this.storySequenceByZoomLevel.put(ZoomLevel.BLOCKS, new StoryTileSequence(ZoomLevel.BLOCKS, startingCoordinates));
        this.storySequenceByZoomLevel.put(ZoomLevel.YEAR, new StoryTileSequence(ZoomLevel.YEAR, startingCoordinates));

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

    public StoryTile getActiveStoryTileAtZoomLevel(GeometryClock.Coords activeFocus, ZoomLevel zoomLevel) {
        this.activeFocusCoordinates = activeFocus;
        return storySequenceByZoomLevel.get(zoomLevel).getActiveStoryTile();
    }


    private class StoryTileSequence {

        private final ZoomLevel zoomLevel;
        private GeometryClock.Coords activeStoryCoordinates;

        private StoryTile activeStoryTile;

        StoryTileSequence(ZoomLevel zoomLevel, GeometryClock.Coords storyCoordinates) {
            this.zoomLevel = zoomLevel;
            this.activeStoryCoordinates = storyCoordinates;
            this.activeStoryTile = new StoryTile(feedUri, storyCoordinates, zoomLevel);

            CarryOverContext context = tileLoader.getContextOfPreviousTile(torchieId, storyCoordinates, zoomLevel);
            if (context != null) {
                activeStoryTile.carryOverFrameContext(context);
            }
        }

        void nextFrame() {
            this.activeStoryCoordinates = activeStoryCoordinates.panRight(zoomLevel);

            StoryTile nextFrame = new StoryTile(feedUri, activeStoryCoordinates, zoomLevel);

            nextFrame.carryOverFrameContext(this.activeStoryTile.getCarryOverContext());
            this.activeStoryTile = nextFrame;

        }

        StoryTile getActiveStoryTile() {
            return this.activeStoryTile;
        }
    }

}
