package com.dreamscale.htmflow.core.feeds.story.music.clock;

import com.dreamscale.htmflow.core.feeds.story.music.BeatSize;
import com.dreamscale.htmflow.core.feeds.story.tile.ZoomLevel;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Iterator;

public class RelativeGeometryClock implements Iterator<RelativeBeat> {

    private final ZoomLevel zoomLevel;
    private Duration relativeStart;
    private Duration relativeEnd;

    private RelativeBeat[] clockBeats;

    private int beatsPerMeasure;

    private int currentBeatIndex;
    private RelativeBeat currentBeat;

    private RelativeGeometryClock summaryClock;

    public RelativeGeometryClock(ZoomLevel zoomLevel, Duration relativeStart, Duration relativeEnd, int beatsPerMeasure ) {

        this.zoomLevel = zoomLevel;
        this.relativeStart = relativeStart;
        this.relativeEnd = relativeEnd;

        this.beatsPerMeasure = beatsPerMeasure;

        this.clockBeats = generateClockBeats(relativeStart, relativeEnd, beatsPerMeasure);
        this.currentBeatIndex = 0;
        this.currentBeat = clockBeats[0];

        summaryClock = toSummaryClock();

    }

    public RelativeGeometryClock toSummaryClock() {
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

    public RelativeBeat getCurrentBeat() {
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

    private RelativeBeat[] generateClockBeats(Duration fromRelativeStart, Duration toRelativeEnd, int beatsPerMeasure) {
        RelativeBeat[] allClockBeats = new RelativeBeat[beatsPerMeasure];
        Beat[] allBaseBeats = Beat.getFlyweightBeats(beatsPerMeasure);

        Duration beatSize = toRelativeEnd.dividedBy(beatsPerMeasure);

        Duration currentBeatTime = fromRelativeStart;

        for (int i = 0; i < beatsPerMeasure; i++) {
            allClockBeats[i] = new RelativeBeat(currentBeatTime, allBaseBeats[i], null);
            currentBeatTime = currentBeatTime.plus(beatSize);
        }

        return allClockBeats;
    }

    private RelativeGeometryClock generateSummaryClock() {
        int summaryBeatsPerMeasure =  beatsPerMeasure / zoomLevel.getBeatsPerPartialSum();

        RelativeGeometryClock summaryClock = new RelativeGeometryClock(zoomLevel, relativeStart, relativeEnd, summaryBeatsPerMeasure);

        for (int i = 0; i < clockBeats.length; i++) {
            RelativeBeat detailBeat = clockBeats[i];

            RelativeBeat summaryBeat = summaryClock.findContainingAngleBeat(detailBeat);
            detailBeat.setToSummaryBeat(summaryBeat);
        }

        return summaryClock;
    }

    private RelativeBeat gotoIndex(int index) {
        if (index < beatsPerMeasure) {
            currentBeatIndex = index;
            currentBeat = clockBeats[currentBeatIndex];
        } else {
            currentBeatIndex = beatsPerMeasure - 1;
            currentBeat = clockBeats[currentBeatIndex];
        }

        return currentBeat;
    }

    public RelativeBeat getClosestBeat(Duration anotherTimeWithinMeasure) {
        RelativeBeat closestBeat = clockBeats[0];
        for (int i = 0 ; i < clockBeats.length; i++) {
            if (clockBeats[i].isBeforeOrEqual(anotherTimeWithinMeasure)) {
                closestBeat = clockBeats[i];
            } else {
                break;
            }
        }

        return closestBeat;
    }

    public Duration getRelativeStart() {
        return relativeStart;
    }

    public Duration getRelativeEnd() {
        return relativeEnd;
    }

    public int getBeats() {
        return beatsPerMeasure;
    }


}
