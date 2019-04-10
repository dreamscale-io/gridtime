package com.dreamscale.htmflow.core.feeds.story;

import com.dreamscale.htmflow.core.feeds.clock.ZoomLevel;
import com.dreamscale.htmflow.core.feeds.clock.GeometryClock;

import java.util.LinkedList;

public class StoryFrameSequence {

        private final ZoomLevel zoomLevel;
        private final GeometryClock.Coords activeStoryCoordinates;
        private final String feedUri;


    LinkedList<StoryFrame> storyFrames;

        StoryFrame activeStoryFrame;

        public StoryFrameSequence(String feedUri, ZoomLevel zoomLevel, GeometryClock.Coords storyCoordinates) {
            this.feedUri = feedUri;
            this.zoomLevel = zoomLevel;
            this.activeStoryCoordinates = storyCoordinates;
            this.activeStoryFrame = new StoryFrame(feedUri, storyCoordinates, zoomLevel);

            this.storyFrames = new LinkedList<>();
            this.storyFrames.add(activeStoryFrame);
        }

        public void nextFrame() {
            StoryFrame nextFrame = new StoryFrame(feedUri, activeStoryCoordinates.panRight(zoomLevel), zoomLevel);

            nextFrame.carryOverFrameContext(this.activeStoryFrame);
            this.storyFrames.add(nextFrame);
            this.activeStoryFrame = nextFrame;

            pruneFramesNotNeededForAggregate();
        }

        private void pruneFramesNotNeededForAggregate() {
            int numberToKeep = zoomLevel.getNumberToKeepForParent();

            if (storyFrames.size() > numberToKeep) {
                storyFrames.removeFirst();
            }
        }

        public StoryFrame getActiveStoryFrame() {
            return this.activeStoryFrame;
        }
}
