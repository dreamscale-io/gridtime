package com.dreamscale.htmflow.core.feeds.story.music;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.hash.HashCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import org.apache.commons.lang3.builder.HashCodeBuilder;

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
public class MusicClock {

    private final LocalDateTime fromClockTime;
    private final LocalDateTime toClockTime;
    private final Duration beatSize;
    private final Duration quarterSize;
    private final Duration halfSize;

    private LocalDateTime currentMoment;

    private Beat beat;

    public MusicClock(LocalDateTime fromClockTime, LocalDateTime toClockTime) {
        this.fromClockTime = fromClockTime;
        this.toClockTime = toClockTime;

        this.beatSize = Duration.between(fromClockTime, toClockTime).dividedBy(BeatSize.BEAT.getBeatCount());
        this.quarterSize = beatSize.multipliedBy( BeatSize.QUARTER.getBeatCount());
        this.halfSize = beatSize.multipliedBy( BeatSize.HALF.getBeatCount());

        this.currentMoment = fromClockTime;
        this.beat = createMusicBeat(currentMoment);
    }

    public Beat next(BeatSize beatIncrementSize) {
        if (beatIncrementSize == BeatSize.BEAT) {
            currentMoment = plusBeat();
        }
        if (beatIncrementSize == BeatSize.QUARTER) {
            currentMoment = plusQuarter();
        }
        if (beatIncrementSize == BeatSize.HALF) {
            currentMoment = plusHalf();
        }
        beat = createMusicBeat(currentMoment);

        return beat;
    }


    public void reset() {
        currentMoment = fromClockTime;
        beat = this.createBeat(currentMoment);
    }

    public Beat createBeat(LocalDateTime moment) {
        return createMusicBeat(moment);
    }

    public Beat getCurrentBeat() {
        return beat;
    }

    private Beat createMusicBeat(LocalDateTime nextClockTime) {

        long secondsIntoMeasure = Duration.between(this.fromClockTime, nextClockTime).getSeconds();
        int beatsIntoMeasure = (int)(secondsIntoMeasure / beatSize.getSeconds());

        int floorBeats = beatsIntoMeasure + 1;
        if (secondsIntoMeasure == 20 * beatSize.getSeconds()) {
            floorBeats = 20;
        }

        return new Beat(nextClockTime,
                floorBeats,
                BeatSize.BEAT.getBeatCount());
    }

    public LocalDateTime getFromClockTime() {
        return fromClockTime;
    }

    public LocalDateTime getToClockTime() {
        return toClockTime;
    }

    public int getBeats() {
        return BeatSize.BEAT.getBeatCount();
    }

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
    public static class Beat {

        @JsonIgnore
        final LocalDateTime clockTime;

        final int beat;
        final int beatsPerMeasure;

        @Override
        public boolean equals(Object o) {
            if (o instanceof Beat) {
                Beat otherBeat = (Beat)o;
                return otherBeat.beat == beat && otherBeat.beatsPerMeasure == beatsPerMeasure;
            }
            return false;
        }

        @Override
        public int hashCode() {
            return (beat + "/" + beatsPerMeasure).hashCode();
        }

        @JsonIgnore
        public Beat toQuarter() {
            int quarter = (beat - 1) / (beatsPerMeasure / 4) + 1;
            return new Beat(clockTime, quarter, 4);
        }

        @JsonIgnore
        public Beat toHalf() {
            int half = (beat - 1) / (beatsPerMeasure / 2) + 1;
            return new Beat(clockTime, half, 2);
        }

        public Beat toBeatSize(BeatSize beatSize) {
            switch (beatSize) {
                case QUARTER:
                    return toQuarter();
                case HALF:
                    return toHalf();
            }
            return this;
        }


        public boolean isWithin(Beat withinBeat) {
            //beats will have different denominators, so need to shrink to the lowest, then compare

            if (withinBeat.beatsPerMeasure == 20) {
                return this.equals(withinBeat);
            }
            else if (withinBeat.beatsPerMeasure == 4) {
                return toQuarter().equals(withinBeat);
            }
            else if (withinBeat.beatsPerMeasure == 2) {
                return toHalf().equals(withinBeat);
            }

            return false;
        }


    }

}
