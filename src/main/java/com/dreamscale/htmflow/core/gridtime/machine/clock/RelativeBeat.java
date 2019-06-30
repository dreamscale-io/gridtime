package com.dreamscale.htmflow.core.gridtime.machine.clock;

import com.dreamscale.htmflow.core.gridtime.capabilities.cmd.returns.Observable;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.ToString;

import java.time.Duration;

@ToString
public class RelativeBeat implements Observable {

    private final Duration relativeDuration;
    private final Beat flyweightBeat;

    private RelativeBeat toSummaryBeat;

    public RelativeBeat(Duration relativeDuration, Beat flyweightBeat) {
        this.relativeDuration = relativeDuration;
        this.flyweightBeat = flyweightBeat;
    }


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

    public int getBeatsPerMeasure() {
        return flyweightBeat.getBeatsPerMeasure();
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

    public String toDisplayString() {
        return flyweightBeat.toDisplayString();
    }
}
