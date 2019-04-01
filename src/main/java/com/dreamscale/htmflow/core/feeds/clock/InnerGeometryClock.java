package com.dreamscale.htmflow.core.feeds.clock;

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
public class InnerGeometryClock {

    private final LocalDateTime fromClockTime;
    private final LocalDateTime toClockTime;
    private final Duration beatSize;
    private final Duration quarterSize;
    private final Duration halfSize;

    private LocalDateTime currentMoment;

    private Coords coords;

    public InnerGeometryClock(LocalDateTime fromClockTime, LocalDateTime toClockTime) {
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

    public Coords createCoords(LocalDateTime moment) {
        return createGeometryCoords(moment);
    }

    public Coords getCoordinates() {
        return coords;
    }

    private Coords createGeometryCoords(LocalDateTime nextClockTime) {

        long secondsIntoMeasure = Duration.between(this.fromClockTime, nextClockTime).getSeconds();
        int beats = (int)(secondsIntoMeasure / beatSize.getSeconds());

        int quarterNotes = beats / BeatsPerBucket.QUARTER.getBeatCount();
        int halfNotes = beats / BeatsPerBucket.HALF.getBeatCount();

        return new Coords(nextClockTime,
                beats,
                quarterNotes,
                halfNotes);
    }

    @AllArgsConstructor
    @Getter
    @ToString
    public class Coords {

        final LocalDateTime clockTime;
        final int beatsIntoMeasure;
        final int quarterNotesIntoMeasure;
        final int halfNotesIntoMeasure;

        public Coords panLeft(BeatsPerBucket beatIncrementSize) {

                switch (beatIncrementSize) {
                    case BEAT:
                        return minusBeat();
                    case QUARTER:
                        return minusQuarter();
                    case HALF:
                        return minusHalf();
                }
                return this;
        }

        public Coords panRight(BeatsPerBucket beatIncrementSize) {

            switch (beatIncrementSize) {
                case BEAT:
                    return plusBeat();
                case QUARTER:
                    return plusQuarter();
                case HALF:
                    return plusHalf();
            }
            return this;
        }

        //pan left functions

        public Coords minusBeat() {
            return createGeometryCoords(clockTime.minus(beatSize));
        }

        public Coords minusQuarter() {
            return createGeometryCoords(clockTime.minus(quarterSize));
        }

        public Coords minusHalf() {
            return createGeometryCoords(clockTime.minus(halfSize));
        }

        // pan right functions

        public Coords plusBeat() {
            return createGeometryCoords(clockTime.plus(beatSize));
        }

        public Coords plusQuarter() {
            return createGeometryCoords(clockTime.plus(quarterSize));
        }

        public Coords plusHalf() {
            return createGeometryCoords(clockTime.plus(halfSize));
        }

    }

}
