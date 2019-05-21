package com.dreamscale.htmflow.core.feeds.executor.parts.pool;

import com.dreamscale.htmflow.core.feeds.clock.GeometryClock;
import com.dreamscale.htmflow.core.feeds.clock.ZoomLevel;
import com.dreamscale.htmflow.core.feeds.executor.parts.fetch.TileLoader;
import com.dreamscale.htmflow.core.feeds.executor.parts.mapper.TileUri;
import com.dreamscale.htmflow.core.feeds.story.StoryTile;
import com.dreamscale.htmflow.core.feeds.story.feature.CarryOverContext;

import java.util.UUID;

/**
 * The pool remembers references from past visited tile operations, we can flesh this out later,
 * for now the pool remembers the last tile in the active sequence
 */

public class SharedFeaturePool {

    private final String feedUri;

    private final TileLoader tileLoader;
    private final UUID torchieId;

    private ZoomLevel activeZoomLevel;
    private final StoryTileSequence activeTileSequence;


    public SharedFeaturePool(UUID torchieId, GeometryClock.Coords startingCoordinates, TileLoader tileLoader) {

        this.torchieId = torchieId;
        this.feedUri = TileUri.createTorchieFeedUri(torchieId);

        this.tileLoader = tileLoader;

        this.activeZoomLevel = ZoomLevel.TWENTIES;
        this.activeTileSequence = new StoryTileSequence(activeZoomLevel, startingCoordinates);

    }

    public void movePosition(GeometryClock.Coords activeCoordinates) {
        activeTileSequence.movePosition(activeCoordinates);
    }


    public StoryTile getActiveStoryTile() {
        return activeTileSequence.getActiveStoryTile();
    }

    public ZoomLevel getActiveZoomLevel() {
        return this.activeZoomLevel;
    }

    public void nextTile() {
        activeTileSequence.nextTile();
    }

    public StoryTile getLastStoryTile() {
        return activeTileSequence.getLastStoryTile();
    }


    private class StoryTileSequence {

        private final ZoomLevel zoomLevel;
        private GeometryClock.Coords activeStoryCoordinates;

        private StoryTile activeStoryTile;
        private StoryTile lastStoryTile;

        StoryTileSequence(ZoomLevel zoomLevel, GeometryClock.Coords storyCoordinates) {
            this.zoomLevel = zoomLevel;
            this.activeStoryCoordinates = storyCoordinates;
            this.activeStoryTile = new StoryTile(feedUri, storyCoordinates, zoomLevel);

            CarryOverContext context = tileLoader.getContextOfPreviousTile(torchieId, storyCoordinates, zoomLevel);
            if (context != null) {
                activeStoryTile.carryOverTileContext(context);
            }
        }

        public void movePosition(GeometryClock.Coords storyCoordinates) {
            this.activeStoryCoordinates = storyCoordinates;
            this.activeStoryTile = new StoryTile(feedUri, storyCoordinates, zoomLevel);

            CarryOverContext context = tileLoader.getContextOfPreviousTile(torchieId, storyCoordinates, zoomLevel);
            if (context != null) {
                activeStoryTile.carryOverTileContext(context);
            }
        }

        void nextTile() {
            lastStoryTile = activeStoryTile;

            this.activeStoryCoordinates = activeStoryCoordinates.panRight(zoomLevel);

            StoryTile nextTile = new StoryTile(feedUri, activeStoryCoordinates, zoomLevel);

            nextTile.carryOverTileContext(this.activeStoryTile.getCarryOverContext());
            this.activeStoryTile = nextTile;

        }

        StoryTile getActiveStoryTile() {
            return this.activeStoryTile;
        }


        public StoryTile getLastStoryTile() {
            return lastStoryTile;
        }
    }

}
