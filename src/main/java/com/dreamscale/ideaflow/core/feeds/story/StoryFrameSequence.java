package com.dreamscale.ideaflow.core.feeds.story;

import com.dreamscale.ideaflow.core.feeds.common.ZoomLevel;
import com.dreamscale.ideaflow.core.feeds.clock.GeometryClock;

import java.util.LinkedList;

public class StoryFrameSequence {

        private final ZoomLevel zoomLevel;
        private final GeometryClock.Coords activeStoryCoordinates;
        LinkedList<StoryFrame> storyFrames;

        StoryFrame activeStoryFrame;

        public StoryFrameSequence(ZoomLevel zoomLevel, GeometryClock.Coords storyCoordinates) {
            this.zoomLevel = zoomLevel;
            this.activeStoryCoordinates = storyCoordinates;
            this.activeStoryFrame = new StoryFrame(storyCoordinates, zoomLevel);

            this.storyFrames = new LinkedList<>();
            this.storyFrames.add(activeStoryFrame);
        }

        public void nextFrame() {
            StoryFrame nextFrame = new StoryFrame(activeStoryCoordinates.panRight(zoomLevel), zoomLevel);

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
