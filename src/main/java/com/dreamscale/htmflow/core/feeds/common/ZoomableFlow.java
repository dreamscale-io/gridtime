package com.dreamscale.htmflow.core.feeds.common;

import com.dreamscale.htmflow.core.feeds.clock.ClockChangeListener;
import com.dreamscale.htmflow.core.feeds.clock.GeometryClock;
import com.dreamscale.htmflow.core.feeds.clock.Metronome;
import com.dreamscale.htmflow.core.feeds.clock.ZoomLevel;
import com.dreamscale.htmflow.core.feeds.executor.parts.pool.SharedFeaturePool;
import com.dreamscale.htmflow.core.feeds.story.StoryTile;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
public class ZoomableFlow implements ClockChangeListener {

    private final Metronome metronome;
    private final UUID torchieId;
    private final SharedFeaturePool sharedFeaturePool;

    private final LinkedList<Runnable> workToDo;

    private Runnable activeWorkItem;
    private Runnable failedWorkItem;

    private int failRetryCount = 0;
    private static final int MAX_RETRIES = 3;

    private ZoomLevel zoomLevel;
    private GeometryClock.Coords activeFocus;


    public ZoomableFlow(UUID torchieId, Metronome metronome, SharedFeaturePool sharedFeaturePool) {
        this.torchieId = torchieId;
        this.metronome = metronome;
        this.sharedFeaturePool = sharedFeaturePool;
        this.workToDo = new LinkedList<>();

        this.metronome.notifyClockTick(this);
    }

    public UUID getTorchieId() {
        return this.torchieId;
    }

    /**
     * One tick of progress, creating a set of work to do
     */
    public void tick() {
        if (workToDo.size() == 0 && metronome.canTick()) {
            workToDo.add(new MetronomeTick(sharedFeaturePool, metronome));
        }
    }

    @Override
    public void onClockTick(ZoomLevel zoomLevel) {
        workToDo.add(new AggregateToZoomLevel(sharedFeaturePool, zoomLevel));
    }

    public Runnable whatsNext() {
        Runnable nextWorkItem = null;

        //short-circuit if we're already busy processing something
        if (activeWorkItem != null) return null;

        //first, retry any failures

        if (failedWorkItem != null && failRetryCount < MAX_RETRIES) {
            failRetryCount++;
            activeWorkItem = failedWorkItem;
            failedWorkItem = null;

            nextWorkItem = activeWorkItem;
        } else if (failedWorkItem != null){
            log.error("Unable to process failure, moving onward...");
            failedWorkItem = null;
            failRetryCount = 0;
        }

        //next, tick onward to process next frame

        if (workToDo.size() == 0) {
            tick();
        }

        if (workToDo.size() > 0) {
            activeWorkItem = workToDo.removeFirst();
            nextWorkItem = activeWorkItem;
        }

        return nextWorkItem;
    }


    public void wrapUpAndBookmark() {

    }

    public StoryTile getActiveStoryTile() {
        return this.sharedFeaturePool.getActiveStoryTile();
    }

    public StoryTile getLastStoryTile() {
        return this.sharedFeaturePool.getLastStoryTile();
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

            //TODO run aggregation job

            activeWorkItem = null;
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
            try {
                LocalDateTime tileTime = metronome.getActiveCoordinates().getClockTime();
                String dreamTime = metronome.getActiveCoordinates().formatDreamTime();

                log.info("Running tick: "+tileTime + " : "+dreamTime);

                this.metronome.tick();
                this.sharedFeaturePool.nextTile();

            } catch (Exception ex) {
                log.error("Exception while processing metronome tick", ex);

                failedWorkItem = this;
            }

            activeWorkItem = null;
        }
    }
}