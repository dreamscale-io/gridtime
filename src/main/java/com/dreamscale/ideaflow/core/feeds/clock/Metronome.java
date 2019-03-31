package com.dreamscale.ideaflow.core.feeds.clock;

import com.dreamscale.ideaflow.core.feeds.common.IdeaFlow;
import com.dreamscale.ideaflow.core.feeds.common.ZoomLevel;
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

    private final List<IdeaFlow> ideaFlowChain;
    private final List<ClockChangeListener> clockChangeListeners;

    public Metronome(LocalDateTime startTime) {
        this.clock = new OuterGeometryClock(startTime);
        this.fromClockPosition = clock.getCoordinates();

        this.ideaFlowChain = new ArrayList<>();
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
        for (IdeaFlow ideaFlow : ideaFlowChain) {
            try {
                ideaFlow.tick(fromClockPosition.getClockTime(), toClockPosition.getClockTime());
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


    public void addFlowToChain(IdeaFlow ideaFlow) {
        this.ideaFlowChain.add(ideaFlow);
    }

    public void notifyClockTick(ClockChangeListener clockChangeListener) {
        this.clockChangeListeners.add(clockChangeListener);
    }

}
