package com.dreamscale.htmflow.core.feeds.story.music.clock;

import com.dreamscale.htmflow.core.feeds.story.music.BeatSize;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Iterator;

public class MusicClock implements Iterator<ClockBeat> {

    private final LocalDateTime fromClockTime;
    private final LocalDateTime toClockTime;

    private ClockBeat[] clockBeats;

    private int beatsPerMeasure;

    private int currentBeatIndex;
    private ClockBeat currentBeat;

    private MusicClock toQuartersClock;

    public MusicClock(LocalDateTime fromClockTime, LocalDateTime toClockTime, int beatsPerMeasure) {

        this.fromClockTime = fromClockTime;
        this.toClockTime = toClockTime;
        this.beatsPerMeasure = beatsPerMeasure;

        this.clockBeats = generateClockBeats(fromClockTime, toClockTime, beatsPerMeasure);
        this.currentBeatIndex = 0;
        this.currentBeat = clockBeats[0];

        if (beatsPerMeasure == 20) {
            toQuartersClock = toQuartersClock();
        }
    }

    public MusicClock toQuartersClock() {
        if (toQuartersClock == null) {
            toQuartersClock = generateQuartersClock(fromClockTime, toClockTime);
        }
        return toQuartersClock;
    }

    private ClockBeat findContainingAngleBeat(ClockBeat detailBeat) {
        ClockBeat containingAngle = null;
        for (int i = 0; i < clockBeats.length; i++) {
            if (detailBeat.isWithin(clockBeats[i])) {
                containingAngle = clockBeats[i];
                break;
            }

        }
        return containingAngle;
    }

    public ClockBeat getCurrentBeat() {
        return currentBeat;
    }

    @Override
    public boolean hasNext() {
        if (currentBeatIndex + 1 > beatsPerMeasure - 1) {
            return false;
        } else {
            return true;
        }
    }

    @Override
    public ClockBeat next() {
        currentBeat = clockBeats[currentBeatIndex];
        currentBeatIndex++;

        return currentBeat;
    }

    public ClockBeat next(BeatSize beatIncrementSize) {
        if (beatIncrementSize == BeatSize.BEAT) {
            return next();
        } else if (beatIncrementSize == BeatSize.QUARTER) {
            return gotoIndex(currentBeatIndex + (beatsPerMeasure / 4));
        } else if (beatIncrementSize == BeatSize.HALF) {
            return gotoIndex(currentBeatIndex + (beatsPerMeasure / 2));
        }
        return currentBeat;
    }

    public void reset() {
        currentBeatIndex = 0;
        currentBeat = clockBeats[0];
    }

    private ClockBeat[] generateClockBeats(LocalDateTime fromClockTime, LocalDateTime toClockTime, int beatsPerMeasure) {
        ClockBeat[] allClockBeats = new ClockBeat[beatsPerMeasure];
        Beat[] allBaseBeats = Beat.getFlyweightBeats(beatsPerMeasure);

        Duration beatSize = Duration.between(fromClockTime, toClockTime).dividedBy(beatsPerMeasure);

        LocalDateTime currentBeatTime = fromClockTime;

        for (int i = 0; i < beatsPerMeasure; i++) {
            allClockBeats[i] = new ClockBeat(currentBeatTime, allBaseBeats[i], null);
            currentBeatTime = currentBeatTime.plus(beatSize);
        }

        return allClockBeats;
    }

    private MusicClock generateQuartersClock(LocalDateTime from, LocalDateTime to) {
        MusicClock quartersClock = new MusicClock(from, to, 4);

        for (int i = 0; i < clockBeats.length; i++) {
            ClockBeat detailBeat = clockBeats[i];

            ClockBeat quarterBeat = quartersClock.findContainingAngleBeat(detailBeat);
            detailBeat.setToQuarter(quarterBeat);
        }

        return quartersClock;
    }

    private ClockBeat gotoIndex(int index) {
        if (index < beatsPerMeasure) {
            currentBeatIndex = index;
            currentBeat = clockBeats[currentBeatIndex];
        } else {
            currentBeatIndex = beatsPerMeasure - 1;
            currentBeat = clockBeats[currentBeatIndex];
        }

        return currentBeat;
    }

    public ClockBeat getClosestBeat(LocalDateTime anotherTimeWithinMeasure) {
        ClockBeat closestBeat = clockBeats[0];
        for (int i = 0 ; i < clockBeats.length; i++) {
            if (clockBeats[i].isBeforeOrEqual(anotherTimeWithinMeasure)) {
                closestBeat = clockBeats[i];
            } else {
                break;
            }
        }

        return closestBeat;
    }

    public LocalDateTime getFromClockTime() {
        return fromClockTime;
    }

    public LocalDateTime getToClockTime() {
        return toClockTime;
    }

    public int getBeats() {
        return beatsPerMeasure;
    }


}
