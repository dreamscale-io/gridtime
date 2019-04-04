package com.dreamscale.htmflow.core.feeds.story.feature.band;

import com.dreamscale.htmflow.core.feeds.clock.InnerGeometryClock;
import com.dreamscale.htmflow.core.feeds.story.feature.FlowFeature;

import java.time.Duration;
import java.time.LocalDateTime;

public class TimeBand implements FlowFeature {

    private final BandContext bandContext;
    private LocalDateTime start;
    private LocalDateTime end;

    private int relativeOffset = 0;

    private InnerGeometryClock.Coords startCoords;
    private InnerGeometryClock.Coords endCoords;

    public TimeBand(LocalDateTime start, LocalDateTime end, BandContext bandContext) {
        this.start = start;
        this.end = end;
        this.bandContext = bandContext;
    }

    public Duration getDuration() {
        return Duration.between(start, end);
    }

    public void setRelativeOffset(int nextSequence) {
        this.relativeOffset = nextSequence;
    }

    public void initCoordinates(InnerGeometryClock clock) {

        this.startCoords = clock.createCoords(start);
        this.endCoords = clock.createCoords(end);
    }

    public LocalDateTime getMoment() {
        return start;
    }

    public int getRelativeOffset() {
        return relativeOffset;
    }

    public InnerGeometryClock.Coords getCoordinates() {
        return this.startCoords;
    }

    public LocalDateTime getEnd() {
        return end;
    }
}
