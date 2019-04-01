package com.dreamscale.htmflow.core.feeds.common;

import com.dreamscale.htmflow.core.feeds.clock.ClockChangeListener;
import com.dreamscale.htmflow.core.feeds.clock.OuterGeometryClock;
import com.dreamscale.htmflow.core.feeds.clock.Metronome;
import com.dreamscale.htmflow.core.feeds.story.StoryFrame;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

@Slf4j
public class ZoomableFlow implements ClockChangeListener {

    private final Metronome metronome;
    private final UUID memberId;
    private final SharedFeaturePool sharedFeaturePool;
    private final LinkedList<Runnable> workToDo;

    private ZoomLevel zoomLevel;
    private OuterGeometryClock.Coords activeFocus;


    public ZoomableFlow(Metronome metronome, UUID memberId, SharedFeaturePool sharedFeaturePool) {
        this.metronome = metronome;
        this.memberId = memberId;
        this.sharedFeaturePool = sharedFeaturePool;
        this.workToDo = new LinkedList<>();

        this.metronome.notifyClockTick(this);
    }

    public UUID getMemberId() {
        return this.memberId;
    }

    /**
     * One tick of progress, creating a set of work to do
     */
    public void tick() {
        if (workToDo.size() == 0) {
            workToDo.add(new MetronomeTick(sharedFeaturePool, metronome));
        }
    }

    @Override
    public void onClockTick(ZoomLevel zoomLevel) {
        workToDo.add(new AggregateToZoomLevel(sharedFeaturePool, zoomLevel));
    }

    public Runnable whatsNext() {
        return workToDo.removeFirst();
    }


    public void wrapUpAndBookmark() {

    }

    public StoryFrame getActiveStoryFrame() {
        return this.sharedFeaturePool.getActiveStoryFrame();
    }

    public StoryFrame zoomIn() {
        zoomLevel = zoomLevel.zoomIn();
        StoryFrame storyFrame =
                this.sharedFeaturePool.getActiveStoryFrameAtZoomLevel(activeFocus, zoomLevel);

        this.activeFocus = storyFrame.getStoryFrameCoordinates();
        return storyFrame;
    }

    public StoryFrame zoomOut() {
        zoomLevel = zoomLevel.zoomOut();
        return this.sharedFeaturePool.getActiveStoryFrameAtZoomLevel(activeFocus, zoomLevel);
    }

    public StoryFrame panLeft() {
        activeFocus = activeFocus.panLeft(zoomLevel);

        return this.sharedFeaturePool.getActiveStoryFrameAtZoomLevel(activeFocus, zoomLevel);
    }

    public StoryFrame panRight() {
        activeFocus = activeFocus.panRight(zoomLevel);

        return this.sharedFeaturePool.getActiveStoryFrameAtZoomLevel(activeFocus, zoomLevel);
    }


    private class AggregateToZoomLevel implements Runnable {


        private final SharedFeaturePool sharedFeaturePool;
        private final ZoomLevel zoomLevel;

        AggregateToZoomLevel(SharedFeaturePool sharedFeaturePool, ZoomLevel zoomLevel) {
            this.sharedFeaturePool = sharedFeaturePool;
            this.zoomLevel = zoomLevel;
        }

        @Override
        public void run() {

            this.sharedFeaturePool.nextFrame(zoomLevel);
            //TODO run aggregation job
        }
    }

    private class MetronomeTick implements Runnable {

        private final Metronome metronome;
        private final SharedFeaturePool sharedFeaturePool;

        MetronomeTick(SharedFeaturePool sharedFeaturePool, Metronome metronome) {
            this.sharedFeaturePool = sharedFeaturePool;
            this.metronome = metronome;
        }

        @Override
        public void run() {
            this.sharedFeaturePool.nextFrame(ZoomLevel.MIN);
            this.metronome.tick();
        }
    }
}
