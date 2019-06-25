package com.dreamscale.htmflow.core.gridtime.kernel.memory.grid.track;

import com.dreamscale.htmflow.core.gridtime.kernel.clock.MusicClock;
import com.dreamscale.htmflow.core.gridtime.kernel.clock.RelativeBeat;
import com.dreamscale.htmflow.core.gridtime.kernel.commons.DefaultCollections;
import com.dreamscale.htmflow.core.gridtime.kernel.memory.grid.cell.*;

import java.util.Iterator;
import java.util.Map;

public class RollingAggregateTrack {

    private final MusicClock summaryClock;

    private Map<RelativeBeat, RollingAggregate> aggregatesPerSummaryBeat = DefaultCollections.map();

    public RollingAggregateTrack(MusicClock musicClock) {
        this.summaryClock = musicClock.toSummaryClock();

        Iterator<RelativeBeat> beatIterator = summaryClock.getForwardsIterator();
        while (beatIterator.hasNext()) {
            RelativeBeat summaryBeat = beatIterator.next();
            aggregatesPerSummaryBeat.put(summaryBeat, new RollingAggregate());
        }
    }

    public void addModificationSample(RelativeBeat beat, int modificationCount) {
        RelativeBeat summaryBeat = beat.toSummaryBeat();

        RollingAggregate aggregate = aggregatesPerSummaryBeat.get(summaryBeat);
        aggregate.addSample(modificationCount);
    }

    public void initCarryOver(RollingAggregate lastRollingAggregate) {
        if (lastRollingAggregate != null) {
            RollingAggregate firstAggregate = getFirst();

            firstAggregate.aggregateWithPastObservations(lastRollingAggregate);
        }
    }

    public void finish() {
        //each 5 minutes, I've got direct samples, and aggregate of the last 20
        Iterator<RelativeBeat> beatIterator = summaryClock.getForwardsIterator();

        RollingAggregate lastAggregate = null;

        while (beatIterator.hasNext()) {
            RelativeBeat summaryBeat = beatIterator.next();
            RollingAggregate aggregate = aggregatesPerSummaryBeat.get(summaryBeat);

            if (lastAggregate != null) {
                aggregate.aggregateWithPastObservations(lastAggregate);
            }
            lastAggregate = aggregate;
        }

    }

    public RollingAggregate getFirst() {
        RelativeBeat firstBeat = summaryClock.getStartBeat();
        return aggregatesPerSummaryBeat.get(firstBeat);
    }

    public RollingAggregate getLast() {
        RelativeBeat lastBeat = summaryClock.getLastBeat();
        return aggregatesPerSummaryBeat.get(lastBeat);
    }

    public RollingAggregate getAggregateAt(RelativeBeat beat) {
        return aggregatesPerSummaryBeat.get(beat);
    }

    public GridRow toGridRow(MetricType metricType, AggregateType aggregateType) {
        Iterator<RelativeBeat> iterator = summaryClock.getForwardsIterator();

        GridRow row = new GridRow(metricType.toDisplayString());

        while (iterator.hasNext()) {
            RelativeBeat summaryBeat = iterator.next();
            CandleStick aggregateCandle = aggregatesPerSummaryBeat.get(summaryBeat).getAggregateCandleStick();

            MetricCell cell = new MetricCell(summaryBeat, metricType, aggregateType, aggregateCandle);
            row.add(cell);
        }
        return row;
    }


}
