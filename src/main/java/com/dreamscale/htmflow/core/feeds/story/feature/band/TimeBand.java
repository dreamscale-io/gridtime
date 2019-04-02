package com.dreamscale.htmflow.core.feeds.story.feature.band;

import com.dreamscale.htmflow.core.feeds.clock.InnerGeometryClock;
import com.dreamscale.htmflow.core.feeds.story.feature.FlowFeature;

import java.time.Duration;
import java.time.LocalDateTime;

public class TimeBand implements FlowFeature {

    private LocalDateTime start;
    private LocalDateTime end;


    private final Object reference;
    private int relativeOffset = 0;

    private InnerGeometryClock.Coords coords;

    public TimeBand(LocalDateTime start, LocalDateTime end, Object reference) {
        this.start = start;
        this.end = end;
        this.reference = reference;
    }

    public Duration getDuration() {
        return Duration.between(start, end);
    }

    public void setRelativeOffset(int nextSequence) {
        this.relativeOffset = nextSequence;
    }

    public void setCoordinates(InnerGeometryClock.Coords coords) {
        this.coords = coords;
    }

    public LocalDateTime getMoment() {
        return start;
    }

    public Object getReference() {
        return reference;
    }

    public int getRelativeOffset() {
        return relativeOffset;
    }

    public boolean referencesType(Class<?> referenceObjectType) {
        return (reference != null && reference.getClass().equals(referenceObjectType));
    }


    public InnerGeometryClock.Coords getCoordinates() {
        return this.coords;
    }

    public LocalDateTime getEnd() {
        return end;
    }
}
