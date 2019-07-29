package com.dreamscale.htmflow.core.gridtime.machine.clock;

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

    private Tick activeTick;
    private boolean hasTicked;

    public Metronome(LocalDateTime startPosition) {

        this.clock = new GeometryClock(startPosition);

        this.fromGridTime = clock.getActiveGridTime();
        this.toGridTime = clock.next();

        activeTick = createTick(fromGridTime, toGridTime);

        this.hasTicked = false;
    }



    public Tick getActiveTick() {
        return activeTick;
    }

    public GeometryClock.GridTime getActivePosition() {
        return fromGridTime;
    }

    public Tick tick() {

        if (hasTicked) {
            GeometryClock.GridTime nextCoordinates = clock.next();

            this.fromGridTime = this.toGridTime;
            this.toGridTime = nextCoordinates;

            this.activeTick = createTick(fromGridTime, toGridTime);
        }
        hasTicked = true;
        
        return activeTick;
    }


    public String getActiveTickPosition() {
        return getActivePosition().toDisplayString();
    }

    public static Tick createTick(ZoomLevel zoomLevel, LocalDateTime fromTime, LocalDateTime toTime) {
        return new Tick(
                GeometryClock.createGridTime(zoomLevel, fromTime),
                GeometryClock.createGridTime(zoomLevel, toTime));
    }

    public static Tick createTick(GeometryClock.GridTime fromGridTime, GeometryClock.GridTime toGridTime) {

        Tick tick = new Tick(fromGridTime, toGridTime);

        if (!Objects.equals(fromGridTime.getDayPart(), toGridTime.getDayPart())) {
            tick.addAggregateTick(getAggregateTick(ZoomLevel.DAY_PART, toGridTime));
        }

        if (!Objects.equals(fromGridTime.getDay(), toGridTime.getDay())) {
            tick.addAggregateTick(getAggregateTick(ZoomLevel.DAY, toGridTime));
        }

        if (!Objects.equals(fromGridTime.getBlockWeek(), toGridTime.getBlockWeek())) {
            tick.addAggregateTick(getAggregateTick(ZoomLevel.WEEK, toGridTime));
        }

        if (!Objects.equals(fromGridTime.getBlock(), toGridTime.getBlock())) {
            tick.addAggregateTick(getAggregateTick(ZoomLevel.BLOCK, toGridTime));
        }
        if (!Objects.equals(fromGridTime.getYear(), toGridTime.getYear())) {
            tick.addAggregateTick(getAggregateTick(ZoomLevel.YEAR, toGridTime));
        }
        return tick;
    }

    private static Tick getAggregateTick(ZoomLevel zoomLevel, GeometryClock.GridTime toGridTime) {
        GeometryClock.GridTime toZoomedOutCoords = toGridTime.toZoomLevel(zoomLevel);
        GeometryClock.GridTime fromZoomedOutCoords = toZoomedOutCoords.panLeft();
        return new Tick(fromZoomedOutCoords, toZoomedOutCoords);
    }

    @Getter
    public static class Tick {
        private final ZoomLevel zoomLevel;
        GeometryClock.GridTime from;
        GeometryClock.GridTime to;

        List<Tick> aggregateTicks;

        Tick(GeometryClock.GridTime from, GeometryClock.GridTime to) {
            this.zoomLevel = from.getZoomLevel();
            this.from = from;
            this.to = to;
        }

        void addAggregateTick(Tick aggregateTick) {
            if (aggregateTicks == null) {
                aggregateTicks = new ArrayList<>();
            }

            aggregateTicks.add(aggregateTick);
        }

        public boolean hasAggregateTicks() {
            return aggregateTicks != null && aggregateTicks.size() > 0;
        }

        public boolean isAfter(LocalDateTime endTime) {
            return from.getClockTime().isAfter(endTime);
        }

        public String toDisplayString() {
            return getFrom().toDisplayString();
        }
    }

}
