package com.dreamscale.htmflow.core.feeds.story;

import com.dreamscale.htmflow.core.feeds.common.ZoomLevel;
import com.dreamscale.htmflow.core.feeds.clock.OuterGeometryClock;

import java.util.LinkedList;

public class StoryTileSequence {

        private final ZoomLevel zoomLevel;
        private final OuterGeometryClock.Coords activeStoryCoordinates;
        LinkedList<StoryTile> storyTiles;

        StoryTile activeStoryTile;

        public StoryTileSequence(ZoomLevel zoomLevel, OuterGeometryClock.Coords storyCoordinates) {
            this.zoomLevel = zoomLevel;
            this.activeStoryCoordinates = storyCoordinates;
            this.activeStoryTile = new StoryTile(storyCoordinates, zoomLevel);

            this.storyTiles = new LinkedList<>();
            this.storyTiles.add(activeStoryTile);
        }

        public void nextFrame() {
            StoryTile nextFrame = new StoryTile(activeStoryCoordinates.panRight(zoomLevel), zoomLevel);

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
