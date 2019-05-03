package com.dreamscale.htmflow.core.feeds.clock;

import com.dreamscale.htmflow.core.feeds.common.Flow;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * All subscribers are guaranteed to receive a tick, before any subscribers get a second tick.
 */
@Slf4j
public class Metronome {

    private GeometryClock clock;

    private GeometryClock.StoryCoords fromClockPosition;
    private GeometryClock.StoryCoords toClockPosition;

    private final List<Flow> flowChain;
    private final List<ClockChangeListener> clockChangeListeners;

    public Metronome(LocalDateTime startTime) {
        this.clock = new GeometryClock(startTime);
        this.fromClockPosition = clock.getCoordinates();

        this.flowChain = new ArrayList<>();
        this.clockChangeListeners = new ArrayList<>();
    }

    public GeometryClock.StoryCoords getActiveCoordinates() {
        return fromClockPosition;
    }

    public void tick() {
        GeometryClock.StoryCoords nextCoordinates = clock.tick();

        this.fromClockPosition = this.toClockPosition;
        this.toClockPosition = nextCoordinates;

        tickForwardFlowChain();

        if (fromClockPosition.fourhours != toClockPosition.fourhours) {
            notifyClockTick(ZoomLevel.FOUR_HOURS);
        }

        if (fromClockPosition.daysIntoWeek != toClockPosition.daysIntoWeek) {
            notifyClockTick(ZoomLevel.DAYS);
        }

        if (fromClockPosition.weeksIntoBlock != toClockPosition.weeksIntoBlock) {
            notifyClockTick(ZoomLevel.WEEKS);
        }

        if (fromClockPosition.block != toClockPosition.block) {
            notifyClockTick(ZoomLevel.BLOCKS);
        }
        if (fromClockPosition.year != toClockPosition.year) {
            notifyClockTick(ZoomLevel.YEAR);
        }
    }

    private void tickForwardFlowChain() {
        for (Flow flow : flowChain) {
            try {
                flow.tick(fromClockPosition.getClockTime(), toClockPosition.getClockTime());
            } catch (InterruptedException ex) {
                log.error("Job interrupted", ex);
            }
        }
    }

    private void notifyClockTick(ZoomLevel zoomLevel)  {
        for (ClockChangeListener listener : clockChangeListeners) {
            try {
                listener.onClockTick(zoomLevel);
            } catch (InterruptedException ex) {
                log.error("Job interrupted", ex);
            }
        }
    }

    public GeometryClock.StoryCoords getFromClockPosition() {
        return this.fromClockPosition;
    }

    public GeometryClock.StoryCoords getToClockPosition() {
        return this.toClockPosition;
    }


    public void addFlowToChain(Flow flow) {
        this.flowChain.add(flow);
    }

    public void notifyClockTick(ClockChangeListener clockChangeListener) {
        this.clockChangeListeners.add(clockChangeListener);
    }

}
