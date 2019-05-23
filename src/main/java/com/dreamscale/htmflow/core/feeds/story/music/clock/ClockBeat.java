package com.dreamscale.htmflow.core.feeds.story.music.clock;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDateTime;

@AllArgsConstructor
@ToString
public class ClockBeat {

    private final LocalDateTime clockTime;
    private final Beat flyweightBeat;

    private ClockBeat toQuarter;

    @Override
    public boolean equals(Object o) {
        return (o instanceof ClockBeat) && ((ClockBeat) o).flyweightBeat == flyweightBeat;
    }

    @Override
    public int hashCode() {
        return flyweightBeat.hashCode();
    }


    public LocalDateTime getClockTime() {
        return clockTime;
    }

    @JsonIgnore
    public int getBeat() {
        return flyweightBeat.getBeat();
    }

    public boolean isWithin(ClockBeat aBiggerBeat) {
        return flyweightBeat.isWithin(aBiggerBeat.flyweightBeat);
    }

    public boolean isBeforeOrEqual(LocalDateTime anotherTimeWithinMeasure) {
        return getClockTime().isBefore(anotherTimeWithinMeasure) || getClockTime().isEqual(anotherTimeWithinMeasure);
    }

    public void setToQuarter(ClockBeat quarterBeat) {
        this.toQuarter = quarterBeat;
    }

    public ClockBeat toQuarter() {
        return toQuarter;
    }
}
