package com.dreamscale.htmflow.core.feeds.clock;

import com.dreamscale.htmflow.core.feeds.common.Flow;
import com.dreamscale.htmflow.core.feeds.common.ZoomLevel;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * All subscribers are guaranteed to receive a tick, before any subscribers get a second tick.
 */
@Slf4j
public class Metronome {

    private OuterGeometryClock clock;

    private OuterGeometryClock.Coords fromClockPosition;
    private OuterGeometryClock.Coords toClockPosition;

    private final List<Flow> flowChain;
    private final List<ClockChangeListener> clockChangeListeners;

    public Metronome(LocalDateTime startTime) {
        this.clock = new OuterGeometryClock(startTime);
        this.fromClockPosition = clock.getCoordinates();

        this.flowChain = new ArrayList<>();
        this.clockChangeListeners = new ArrayList<>();
    }

    public OuterGeometryClock.Coords getActiveCoordinates() {
        return fromClockPosition;
    }

    public void tick() {
        OuterGeometryClock.Coords nextCoordinates = clock.tick();

        this.fromClockPosition = this.toClockPosition;
        this.toClockPosition = nextCoordinates;

        tickForwardFlowChain();

        if (fromClockPosition.hoursIntoDay != toClockPosition.hoursIntoDay) {
            notifyClockTick(ZoomLevel.HOUR);
        }

        if (fromClockPosition.daysIntoWeek != toClockPosition.daysIntoWeek) {
            notifyClockTick(ZoomLevel.DAY);
        }

        if (fromClockPosition.weeksIntoYear != toClockPosition.weeksIntoYear) {
            notifyClockTick(ZoomLevel.WEEK);
        }

        if (fromClockPosition.weeksIntoBlock != toClockPosition.weeksIntoBlock) {
            notifyClockTick(ZoomLevel.BLOCK);
        }
        if (fromClockPosition.currentYear != toClockPosition.currentYear) {
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

    public OuterGeometryClock.Coords getFromClockPosition() {
        return this.fromClockPosition;
    }

    public OuterGeometryClock.Coords getToClockPosition() {
        return this.toClockPosition;
    }


    public void addFlowToChain(Flow flow) {
        this.flowChain.add(flow);
    }

    public void notifyClockTick(ClockChangeListener clockChangeListener) {
        this.clockChangeListeners.add(clockChangeListener);
    }

}
