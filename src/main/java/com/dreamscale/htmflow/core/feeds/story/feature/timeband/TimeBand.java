package com.dreamscale.htmflow.core.feeds.story.feature.timeband;

import com.dreamscale.htmflow.core.feeds.story.music.MusicGeometryClock;
import com.dreamscale.htmflow.core.feeds.story.music.Playable;
import com.dreamscale.htmflow.core.feeds.story.feature.FlowFeature;
import com.dreamscale.htmflow.core.feeds.story.feature.details.Details;

import java.time.Duration;
import java.time.LocalDateTime;

public class TimeBand extends FlowFeature implements Playable {

    private Details details;
    private LocalDateTime start;
    private LocalDateTime end;

    private int relativeOffset = 0;

    private MusicGeometryClock.Coords startCoords;
    private MusicGeometryClock.Coords endCoords;

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

    public void initCoordinates(MusicGeometryClock clock) {

        this.startCoords = clock.createCoords(start);
        this.endCoords = clock.createCoords(end);
    }

    public MusicGeometryClock.Coords getStartCoords() {
        return this.startCoords;
    }

    public MusicGeometryClock.Coords getEndCoords() {
        return this.endCoords;
    }

    public LocalDateTime getMoment() {
        return start;
    }

    public int getRelativeOffset() {
        return relativeOffset;
    }

    public MusicGeometryClock.Coords getCoordinates() {
        return this.startCoords;
    }

    public LocalDateTime getStart() {
        return start;
    }
    public LocalDateTime getEnd() {
        return end;
    }

    public boolean contains(LocalDateTime moment) {
        return (moment.isEqual(getStart()) || moment.isAfter(getStart())) && moment.isBefore(getEnd());
    }

    public void setDetails(Details details) {
        this.details = details;
    }

    public Details getDetails() {
        return details;
    }
}
