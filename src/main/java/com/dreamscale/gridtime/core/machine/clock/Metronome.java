package com.dreamscale.gridtime.core.machine.clock;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Slf4j
public class Metronome {


    private GeometryClock clock;

    private GeometryClock.GridTime fromGridTime;
    private GeometryClock.GridTime toGridTime;

    private TickScope activeTickScope;
    private boolean hasTicked;

    public Metronome(LocalDateTime startPosition) {

        this.clock = new GeometryClock(startPosition);

        this.fromGridTime = clock.getActiveGridTime();
        this.toGridTime = clock.next();

        activeTickScope = createTick(fromGridTime, toGridTime);

        this.hasTicked = false;
    }


    public TickScope getActiveTick() {
        return activeTickScope;
    }

    public GeometryClock.GridTime getActivePosition() {
        return fromGridTime;
    }

    public TickScope tick() {

        if (hasTicked) {
            GeometryClock.GridTime nextCoordinates = clock.next();

            this.fromGridTime = this.toGridTime;
            this.toGridTime = nextCoordinates;

            this.activeTickScope = createTick(fromGridTime, toGridTime);
        }
        hasTicked = true;

        return activeTickScope;
    }

    public String getActiveTickPosition() {
        return getActivePosition().toDisplayString();
    }

    public static TickScope createTick(ZoomLevel zoomLevel, LocalDateTime fromTime, LocalDateTime toTime) {
        return new TickScope(
                GeometryClock.createGridTime(zoomLevel, fromTime),
                GeometryClock.createGridTime(zoomLevel, toTime));
    }

    public static TickScope createTick(GeometryClock.GridTime fromGridTime) {
        return createTick(fromGridTime, fromGridTime.panRight());
    }

    public static TickScope createTick(GeometryClock.GridTime fromGridTime, GeometryClock.GridTime toGridTime) {

        TickScope tickScope = new TickScope(fromGridTime, toGridTime);

        if (!Objects.equals(fromGridTime.getDayPart(), toGridTime.getDayPart())) {
            tickScope.addAggregateTick(getAggregateTick(ZoomLevel.DAY_PART, toGridTime));
        }

        if (!Objects.equals(fromGridTime.getDay(), toGridTime.getDay())) {
            tickScope.addAggregateTick(getAggregateTick(ZoomLevel.DAY, toGridTime));
        }

        if (!Objects.equals(fromGridTime.getBlockWeek(), toGridTime.getBlockWeek())) {
            tickScope.addAggregateTick(getAggregateTick(ZoomLevel.WEEK, toGridTime));
        }

        if (!Objects.equals(fromGridTime.getBlock(), toGridTime.getBlock())) {
            tickScope.addAggregateTick(getAggregateTick(ZoomLevel.BLOCK, toGridTime));
        }
        if (!Objects.equals(fromGridTime.getYear(), toGridTime.getYear())) {
            tickScope.addAggregateTick(getAggregateTick(ZoomLevel.YEAR, toGridTime));
        }
        return tickScope;
    }

    private static TickScope getAggregateTick(ZoomLevel zoomLevel, GeometryClock.GridTime toGridTime) {
        GeometryClock.GridTime toZoomedOutCoords = toGridTime.toZoomLevel(zoomLevel);
        GeometryClock.GridTime fromZoomedOutCoords = toZoomedOutCoords.panLeft();
        return new TickScope(fromZoomedOutCoords, toZoomedOutCoords);
    }

    public void sync(TickScope lastTickScope) {
        if (lastTickScope != null && activeTickScope != lastTickScope) {
            clock.sync(lastTickScope.from);

            fromGridTime = lastTickScope.from;
            toGridTime = lastTickScope.to;

            activeTickScope = lastTickScope;
        }
    }

    @Getter
    public static class TickScope {
        private final ZoomLevel zoomLevel;
        GeometryClock.GridTime from;
        GeometryClock.GridTime to;

        List<TickScope> aggregateTickScopes;

        public TickScope(GeometryClock.GridTime from, GeometryClock.GridTime to) {
            this.zoomLevel = from.getZoomLevel();
            this.from = from;
            this.to = to;
        }

        void addAggregateTick(TickScope aggregateTickScope) {
            if (aggregateTickScopes == null) {
                aggregateTickScopes = new ArrayList<>();
            }

            aggregateTickScopes.add(aggregateTickScope);
        }

        public boolean hasAggregateTicks() {
            return aggregateTickScopes != null && aggregateTickScopes.size() > 0;
        }

        public boolean isAfter(LocalDateTime endTime) {
            return from.getClockTime().isAfter(endTime);
        }

        public String toDisplayString() {
            return getFrom().toDisplayString();
        }
    }

}
