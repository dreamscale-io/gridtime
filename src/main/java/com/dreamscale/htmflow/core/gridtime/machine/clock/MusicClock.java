package com.dreamscale.htmflow.core.gridtime.machine.clock;

import com.dreamscale.htmflow.core.gridtime.machine.executor.alarm.TimeBombTrigger;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.Iterator;

@Slf4j
public class MusicClock implements Iterator<RelativeBeat> {

    private final ZoomLevel zoomLevel;
    private Duration relativeStart;
    private Duration relativeEnd;

    private RelativeBeat[] clockBeats;

    private int beatsPerMeasure;

    private int currentBeatIndex;
    private RelativeBeat currentBeat;

    private MusicClock summaryClock;

    public MusicClock(ZoomLevel zoomLevel) {
        this(zoomLevel, zoomLevel.getInnerBeats());
    }

    private MusicClock(ZoomLevel zoomLevel, int beats) {

        this.zoomLevel = zoomLevel;
        this.relativeStart = Duration.ofSeconds(0);
        this.relativeEnd = zoomLevel.getDuration();
        this.beatsPerMeasure = beats;

        initializeClockBeats(relativeStart, relativeEnd, beats);

        this.currentBeatIndex = 0;
        this.currentBeat = clockBeats[0];

        if (zoomLevel.getInnerBeats() == beats) {
            summaryClock = toSummaryClock();
        }
    }

    public MusicClock toSummaryClock() {
        if (summaryClock == null) {
            summaryClock = generateSummaryClock();
        }
        return summaryClock;
    }


    private RelativeBeat findContainingAngleBeat(RelativeBeat detailBeat) {
        RelativeBeat containingAngle = null;
        for (int i = 0; i < clockBeats.length; i++) {
            if (detailBeat.isWithin(clockBeats[i])) {
                containingAngle = clockBeats[i];
                break;
            }

        }
        return containingAngle;
    }

    public RelativeBeat getActiveBeat() {
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
    public RelativeBeat next() {
        currentBeat = clockBeats[currentBeatIndex];
        currentBeatIndex++;

        return currentBeat;
    }

    public void reset() {
        currentBeatIndex = 0;
        currentBeat = clockBeats[0];
    }

    private void initializeClockBeats(Duration fromRelativeStart, Duration toRelativeEnd, int beatsPerMeasure) {
        RelativeBeat[] allClockBeats = new RelativeBeat[beatsPerMeasure];
        Beat[] allBaseBeats = Beat.getFlyweightBeats(beatsPerMeasure);

        Duration beatSize = toRelativeEnd.dividedBy(beatsPerMeasure);

        Duration currentBeatTime = fromRelativeStart;

        for (int i = 0; i < beatsPerMeasure; i++) {
            allClockBeats[i] = new RelativeBeat(currentBeatTime, allBaseBeats[i]);
            currentBeatTime = currentBeatTime.plus(beatSize);
        }

        clockBeats = allClockBeats;
    }

    private MusicClock generateSummaryClock() {
        int summaryBeatsPerMeasure =  beatsPerMeasure / zoomLevel.getBeatsPerPartialSum();

        MusicClock summaryClock = new MusicClock(zoomLevel, summaryBeatsPerMeasure);

        for (int i = 0; i < clockBeats.length; i++) {
            RelativeBeat detailBeat = clockBeats[i];

            RelativeBeat summaryBeat = summaryClock.findContainingAngleBeat(detailBeat);
            detailBeat.setToSummaryBeat(summaryBeat);
        }

        return summaryClock;
    }

    public RelativeBeat gotoBeat(int beatNumber) {
        if (beatNumber <= beatsPerMeasure) {
            currentBeatIndex = beatNumber - 1;
            currentBeat = clockBeats[currentBeatIndex];
        } else {
            currentBeatIndex = beatsPerMeasure - 1;
            currentBeat = clockBeats[currentBeatIndex];
        }

        return currentBeat;
    }

    public boolean isWithinMeasure(Duration relativeTime) {
        return relativeTime.getSeconds() < relativeEnd.getSeconds();
    }

    public RelativeBeat getPreviousBeat(RelativeBeat fromBeat) {
        int prevBeatNumber = fromBeat.getBeat() - 1;
        if (prevBeatNumber > 0) {
            return getBeat(prevBeatNumber);
        } else {
            return fromBeat;
        }
    }

    public RelativeBeat getClosestBeat(Duration aRandomTimeWithinMeasure) {
        RelativeBeat closestBeat = clockBeats[0];
        for (int i = 0 ; i < clockBeats.length; i++) {
            if (clockBeats[i].isBeforeOrEqual(aRandomTimeWithinMeasure)) {
                closestBeat = clockBeats[i];
            } else {
                break;
            }
        }

        return closestBeat;
    }

    public TimeBombTrigger getFutureTimeBomb(Duration futureTime) {
        int fullMeasuresToCountDown = (int)Math.floorDiv(futureTime.getSeconds() , relativeEnd.getSeconds() );
        int secondsOffset = (int)Math.floorMod(futureTime.getSeconds(), relativeEnd.getSeconds());

        RelativeBeat beatWithinMeasureToSplode = getClosestBeat(Duration.ofSeconds(secondsOffset));

        return new TimeBombTrigger(zoomLevel, fullMeasuresToCountDown, beatWithinMeasureToSplode);
    }


    public Duration getRelativeStart() {
        return relativeStart;
    }

    public Duration getRelativeEnd() {
        return relativeEnd;
    }

    public int getBeatsPerMeasure() {
        return beatsPerMeasure;
    }

    public RelativeBeat getBeat(int beatNumber) {
        return clockBeats[beatNumber - 1];
    }

    public RelativeBeat[] getClockBeats() {
        return clockBeats;
    }


    public RelativeBeat getStartBeat() {
        return clockBeats[0];
    }

    public RelativeBeat getLastBeat() {
        return clockBeats[clockBeats.length - 1];
    }

    public Iterator<RelativeBeat> getBackwardsIterator() {
        return getBackwardsIterator(getLastBeat());
    }

    public Iterator<RelativeBeat> getBackwardsIterator(RelativeBeat startFromBeat) {

        return new Iterator<RelativeBeat>() {
            int activeIndex = startFromBeat.getBeat() - 1;

            @Override
            public boolean hasNext() {
                return (activeIndex >= 0);
            }

            @Override
            public RelativeBeat next() {
                RelativeBeat backwardsBeat = clockBeats[activeIndex];
                activeIndex--;

                return backwardsBeat;
            }
        };
    }

    public Iterator<RelativeBeat> getForwardsIterator() {

        return new Iterator<RelativeBeat>() {
            int activeIndex = 0;

            @Override
            public boolean hasNext() {
                return (activeIndex < clockBeats.length);
            }

            @Override
            public RelativeBeat next() {

                RelativeBeat forwardsBeat = clockBeats[activeIndex];
                activeIndex++;

                return forwardsBeat;
            }
        };
    }


}
