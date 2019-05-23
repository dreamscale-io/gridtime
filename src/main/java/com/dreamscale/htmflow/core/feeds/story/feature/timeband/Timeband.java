package com.dreamscale.htmflow.core.feeds.story.feature.timeband;

import com.dreamscale.htmflow.core.domain.tile.FlowObjectType;
import com.dreamscale.htmflow.core.feeds.story.music.clock.ClockBeat;
import com.dreamscale.htmflow.core.feeds.story.music.clock.MusicClock;
import com.dreamscale.htmflow.core.feeds.story.music.Playable;
import com.dreamscale.htmflow.core.feeds.story.feature.FlowFeature;
import com.dreamscale.htmflow.core.feeds.story.feature.details.Details;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import java.time.Duration;
import java.time.LocalDateTime;

@Getter
@Setter
public class Timeband extends FlowFeature implements Playable {

    private Details details;
    private LocalDateTime start;
    private LocalDateTime end;

    private int relativeSequence = 0;

    private ClockBeat startClockBeat;
    private ClockBeat endClockBeat;

    public Timeband(LocalDateTime start, LocalDateTime end, Details details) {
        this();
        this.start = start;
        this.end = end;
        this.details = details;
    }

    public Timeband() {
        super(FlowObjectType.TIMEBAND);
    }

    @JsonIgnore
    public Duration getDuration() {
        return Duration.between(start, end);
    }

    public void initRelativeSequence(TimebandLayer layer, int nextSequence) {
        relativeSequence = nextSequence;

        setRelativePath("/band/"+nextSequence);
        setUri(layer.getUri() + getRelativePath());
    }

    public void initCoordinates(MusicClock clock) {

        this.startClockBeat = clock.getClosestBeat(start);
        this.endClockBeat = clock.getClosestBeat(end);
    }

    @JsonIgnore
    public LocalDateTime getMoment() {
        return start;
    }

    @JsonIgnore
    public ClockBeat getCoordinates() {
        return this.startClockBeat;
    }

    public boolean contains(LocalDateTime moment) {
        return (moment.isEqual(getStart()) || moment.isAfter(getStart())) && moment.isBefore(getEnd());
    }

}
