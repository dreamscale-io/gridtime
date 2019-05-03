package com.dreamscale.htmflow.core.feeds.story.music;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Unlike the outer geometry clock that uses the passing of absolute time to judge timings,
 * the inner clock is a relative clocking that measures the distance between the start and ending times of a sequence,
 * then divides up the duration into even fractional parts, similar to the way a measure in music, is divided
 * into parts to give us the concept of a whole note, half note, quarter note, in musical notation.
 *
 * 20 beats per measure, 5 beats per quarter note.
 *
 * For an inner geometry clock aligned to the start and end of a StoryFrame, the inner clock
 * will align with also align with absolute time, such that each beat is 1 minute, and a quarter note is 5 minutes.
 *
 */
public class MusicGeometryClock {

    private final LocalDateTime fromClockTime;
    private final LocalDateTime toClockTime;
    private final Duration beatSize;
    private final Duration quarterSize;
    private final Duration halfSize;

    private LocalDateTime currentMoment;

    private Coords coords;

    public MusicGeometryClock(LocalDateTime fromClockTime, LocalDateTime toClockTime) {
        this.fromClockTime = fromClockTime;
        this.toClockTime = toClockTime;

        this.beatSize = Duration.between(fromClockTime, toClockTime).dividedBy(BeatsPerBucket.BEAT.getBeatCount());
        this.quarterSize = beatSize.multipliedBy( BeatsPerBucket.QUARTER.getBeatCount());
        this.halfSize = beatSize.multipliedBy( BeatsPerBucket.HALF.getBeatCount());

        this.currentMoment = fromClockTime;
        this.coords = createGeometryCoords(currentMoment);
    }

    public Coords tick() {
        LocalDateTime nextClockTime = this.currentMoment.plus(beatSize);

        this.coords = createGeometryCoords(nextClockTime);
        this.currentMoment = nextClockTime;

        return this.coords;
    }

    public Coords panLeft(BeatsPerBucket beatIncrementSize) {
        if (beatIncrementSize == BeatsPerBucket.BEAT) {
            currentMoment = minusBeat();
        }
        if (beatIncrementSize == BeatsPerBucket.QUARTER) {
            currentMoment = minusQuarter();
        }
        if (beatIncrementSize == BeatsPerBucket.HALF) {
            currentMoment = minusHalf();
        }
        coords = createGeometryCoords(currentMoment);

        return coords;
    }

    public Coords panRight(BeatsPerBucket beatIncrementSize) {
        if (beatIncrementSize == BeatsPerBucket.BEAT) {
            currentMoment = plusBeat();
        }
        if (beatIncrementSize == BeatsPerBucket.QUARTER) {
            currentMoment = plusQuarter();
        }
        if (beatIncrementSize == BeatsPerBucket.HALF) {
            currentMoment = plusHalf();
        }
        coords = createGeometryCoords(currentMoment);

        return coords;
    }


    public void reset() {
        currentMoment = fromClockTime;
        coords = this.createCoords(currentMoment);
    }

    public Coords createCoords(LocalDateTime moment) {
        return createGeometryCoords(moment);
    }

    public Coords getCoordinates() {
        return coords;
    }

    private Coords createGeometryCoords(LocalDateTime nextClockTime) {

        long secondsIntoMeasure = Duration.between(this.fromClockTime, nextClockTime).getSeconds();
        int beatsIntoMeasure = (int)(secondsIntoMeasure / beatSize.getSeconds());

        int floorBeats = beatsIntoMeasure + 1;
        if (secondsIntoMeasure == 20 * beatSize.getSeconds()) {
            floorBeats = 20;
        }

        return new Coords(nextClockTime, secondsIntoMeasure,
                floorBeats,
                BeatsPerBucket.BEAT.getBeatCount());
    }

    public LocalDateTime getFromClockTime() {
        return fromClockTime;
    }

    public LocalDateTime getToClockTime() {
        return toClockTime;
    }

    public int getBeats() {
        return BeatsPerBucket.BEAT.getBeatCount();
    }



    //pan left functions

    private LocalDateTime minusBeat() {
        return currentMoment.minus(beatSize);
    }

    private LocalDateTime minusQuarter() {
        return currentMoment.minus(quarterSize);
    }

    private LocalDateTime minusHalf() {
        return currentMoment.minus(halfSize);
    }

    // pan right functions

    private LocalDateTime plusBeat() {
        return currentMoment.plus(beatSize);
    }

    private LocalDateTime plusQuarter() {
        return currentMoment.plus(quarterSize);
    }

    private LocalDateTime plusHalf() {
        return currentMoment.plus(halfSize);
    }


    @AllArgsConstructor
    @Getter
    @ToString
    public static class Coords {

        @JsonIgnore
        final LocalDateTime clockTime;

        final long secondsIntoMeasure;
        final int beat;
        final int beatsPerMeasure;

        public boolean isBeforeOrEqual(Coords coords) {
            return beat <= coords.beat;
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof Coords) {
                return ((Coords)o).beat == beat;
            }
            return false;
        }

        @JsonIgnore
        public int getQuarter() {
            return (beat - 1) / (beatsPerMeasure / 4) + 1;
        }

        @JsonIgnore
        public int getHalf() {
            return (beat - 1) / (beatsPerMeasure / 2) + 1;
        }


        @Override
        public int hashCode() {
            return Integer.valueOf(beat).hashCode();
        }

        public boolean isAfterOrEqual(Coords coords) {
            return beat >= coords.beat;
        }

    }

}
