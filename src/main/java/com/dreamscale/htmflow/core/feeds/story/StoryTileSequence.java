package com.dreamscale.htmflow.core.feeds.story;

import com.dreamscale.htmflow.core.feeds.clock.ZoomLevel;
import com.dreamscale.htmflow.core.feeds.clock.GeometryClock;

import java.util.LinkedList;

public class StoryTileSequence {

        private final ZoomLevel zoomLevel;
        private final GeometryClock.StoryCoords activeStoryCoordinates;
        private final String feedUri;


    LinkedList<StoryTile> storyTiles;

        StoryTile activeStoryTile;

        public StoryTileSequence(String feedUri, ZoomLevel zoomLevel, GeometryClock.StoryCoords storyCoordinates) {
            this.feedUri = feedUri;
            this.zoomLevel = zoomLevel;
            this.activeStoryCoordinates = storyCoordinates;
            this.activeStoryTile = new StoryTile(feedUri, storyCoordinates, zoomLevel);

            this.storyTiles = new LinkedList<>();
            this.storyTiles.add(activeStoryTile);
        }

        public void nextFrame() {
            StoryTile nextFrame = new StoryTile(feedUri, activeStoryCoordinates.panRight(zoomLevel), zoomLevel);

            nextFrame.carryOverFrameContext(this.activeStoryTile);
            this.storyTiles.add(nextFrame);
            this.activeStoryTile = nextFrame;

            pruneFramesNotNeededForAggregate();
        }

        private void pruneFramesNotNeededForAggregate() {
            int numberToKeep = zoomLevel.getNumberToKeepForParent();

            if (storyTiles.size() > numberToKeep) {
                storyTiles.removeFirst();
            }
        }

        public StoryTile getActiveStoryTile() {
            return this.activeStoryTile;
        }
}
