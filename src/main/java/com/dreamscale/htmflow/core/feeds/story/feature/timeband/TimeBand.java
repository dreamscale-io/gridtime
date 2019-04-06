package com.dreamscale.htmflow.core.feeds.story.feature.timeband;

import com.dreamscale.htmflow.core.feeds.clock.InnerGeometryClock;
import com.dreamscale.htmflow.core.feeds.story.feature.FlowFeature;
import com.dreamscale.htmflow.core.feeds.story.feature.details.Details;
import com.dreamscale.htmflow.core.feeds.story.feature.details.ProgressDetails;

import java.time.Duration;
import java.time.LocalDateTime;

public class TimeBand implements FlowFeature {

    private Details details;
    private LocalDateTime start;
    private LocalDateTime end;

    private int relativeOffset = 0;

    private InnerGeometryClock.Coords startCoords;
    private InnerGeometryClock.Coords endCoords;

    public TimeBand(LocalDateTime start, LocalDateTime end, Details details) {
        this.start = start;
        this.end = end;
        this.details = details;
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

    public LocalDateTime getStart() {
        return start;
    }
    public LocalDateTime getEnd() {
        return end;
    }

    public void setDetails(Details details) {
        this.details = details;
    }
}
