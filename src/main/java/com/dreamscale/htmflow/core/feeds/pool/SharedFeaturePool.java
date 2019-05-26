package com.dreamscale.htmflow.core.feeds.pool;

import com.dreamscale.htmflow.core.feeds.clock.GeometryClock;
import com.dreamscale.htmflow.core.feeds.clock.ZoomLevel;
import com.dreamscale.htmflow.core.feeds.story.mapper.TileUri;
import com.dreamscale.htmflow.core.feeds.story.TileBuilder;
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
    private final FeatureCache featureCache;

    private ZoomLevel activeZoomLevel;
    private final StoryTileSequence activeTileSequence;


    public SharedFeaturePool(UUID torchieId, GeometryClock.Coords startingCoordinates, TileLoader tileLoader, FeatureCache featureCache) {

        this.torchieId = torchieId;
        this.feedUri = TileUri.createTorchieFeedUri(torchieId);

        this.tileLoader = tileLoader;
        this.featureCache = featureCache;

        this.activeZoomLevel = ZoomLevel.TWENTIES;
        this.activeTileSequence = new StoryTileSequence(activeZoomLevel, startingCoordinates);

    }

    public void movePosition(GeometryClock.Coords activeCoordinates) {
        activeTileSequence.movePosition(activeCoordinates);
    }


    public TileBuilder getActiveStoryTile() {
        return activeTileSequence.getActiveTileBuilder();
    }

    public ZoomLevel getActiveZoomLevel() {
        return this.activeZoomLevel;
    }

    public void nextTile() {
        activeTileSequence.nextTile();
    }

    public TileBuilder getLastStoryTile() {
        return activeTileSequence.getLastTileBuilder();
    }


    private class StoryTileSequence {

        private final ZoomLevel zoomLevel;
        private GeometryClock.Coords activeStoryCoordinates;

        private TileBuilder activeTileBuilder;
        private TileBuilder lastTileBuilder;

        StoryTileSequence(ZoomLevel zoomLevel, GeometryClock.Coords storyCoordinates) {
            this.zoomLevel = zoomLevel;
            this.activeStoryCoordinates = storyCoordinates;
            this.activeTileBuilder = new TileBuilder(feedUri, storyCoordinates, zoomLevel);

            CarryOverContext context = tileLoader.getContextOfPreviousTile(torchieId, storyCoordinates, zoomLevel);
            if (context != null) {
                activeTileBuilder.carryOverTileContext(context);
            }
        }

        public void movePosition(GeometryClock.Coords storyCoordinates) {
            this.activeStoryCoordinates = storyCoordinates;
            this.activeTileBuilder = new TileBuilder(feedUri, storyCoordinates, zoomLevel);

            CarryOverContext context = tileLoader.getContextOfPreviousTile(torchieId, storyCoordinates, zoomLevel);
            if (context != null) {
                activeTileBuilder.carryOverTileContext(context);
            }
        }

        void nextTile() {
            lastTileBuilder = activeTileBuilder;

            this.activeStoryCoordinates = activeStoryCoordinates.panRight(zoomLevel);

            TileBuilder nextTile = new TileBuilder(feedUri, activeStoryCoordinates, zoomLevel);

            nextTile.carryOverTileContext(this.activeTileBuilder.getCarryOverContext());
            this.activeTileBuilder = nextTile;

        }

        TileBuilder getActiveTileBuilder() {
            return this.activeTileBuilder;
        }


        public TileBuilder getLastTileBuilder() {
            return lastTileBuilder;
        }
    }

}
