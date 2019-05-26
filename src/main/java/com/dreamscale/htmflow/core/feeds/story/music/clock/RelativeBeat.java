package com.dreamscale.htmflow.core.feeds.story.music.clock;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.ToString;

import java.time.Duration;

@AllArgsConstructor
@ToString
public class RelativeBeat {

    private final Duration relativeDuration;
    private final Beat flyweightBeat;

    private RelativeBeat toSummaryBeat;

    @Override
    public boolean equals(Object o) {
        return (o instanceof RelativeBeat) && ((RelativeBeat) o).flyweightBeat == flyweightBeat;
    }

    @Override
    public int hashCode() {
        return flyweightBeat.hashCode();
    }


    public Duration getRelativeDuration() {
        return relativeDuration;
    }

    @JsonIgnore
    public int getBeat() {
        return flyweightBeat.getBeat();
    }

    public boolean isWithin(RelativeBeat aBiggerBeat) {
        return flyweightBeat.isWithin(aBiggerBeat.flyweightBeat);
    }

    public boolean isBeforeOrEqual(Duration anotherRelativeTimeWithinMeasure) {
        long mySeconds = getRelativeDuration().getSeconds();
        long otherSeconds = anotherRelativeTimeWithinMeasure.getSeconds();

        return mySeconds <= otherSeconds;
    }

    public void setToSummaryBeat(RelativeBeat summaryBeat) {
        this.toSummaryBeat = summaryBeat;
    }

    public RelativeBeat toSummaryBeat() {
        return toSummaryBeat;
    }
}
