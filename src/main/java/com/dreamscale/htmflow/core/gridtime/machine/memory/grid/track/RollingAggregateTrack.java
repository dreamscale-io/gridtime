package com.dreamscale.htmflow.core.gridtime.machine.memory.grid.track;

import com.dreamscale.htmflow.core.gridtime.machine.clock.GeometryClock;
import com.dreamscale.htmflow.core.gridtime.machine.clock.MusicClock;
import com.dreamscale.htmflow.core.gridtime.machine.clock.RelativeBeat;
import com.dreamscale.htmflow.core.gridtime.machine.commons.DefaultCollections;
import com.dreamscale.htmflow.core.gridtime.machine.memory.grid.cell.*;
import com.dreamscale.htmflow.core.gridtime.machine.memory.grid.cell.metrics.AggregateType;
import com.dreamscale.htmflow.core.gridtime.machine.memory.grid.cell.metrics.CandleStick;
import com.dreamscale.htmflow.core.gridtime.machine.memory.grid.query.key.MetricRowKey;
import com.dreamscale.htmflow.core.gridtime.machine.memory.grid.cell.metrics.RollingAggregate;
import com.dreamscale.htmflow.core.gridtime.machine.memory.grid.cell.type.MetricCell;

import java.util.Iterator;
import java.util.Map;

public class RollingAggregateTrack {

    private final MusicClock summaryClock;
    private final GeometryClock.GridTime gridTime;

    private Map<RelativeBeat, RollingAggregate> aggregatesPerSummaryBeat = DefaultCollections.map();

    public RollingAggregateTrack(GeometryClock.GridTime gridTime, MusicClock musicClock) {
        this.gridTime = gridTime;
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

    public GridRow toGridRow(MetricRowKey metricRowKey, AggregateType aggregateType) {
        Iterator<RelativeBeat> iterator = summaryClock.getForwardsIterator();

        GridRow row = new GridRow(metricRowKey);

        while (iterator.hasNext()) {
            RelativeBeat summaryBeat = iterator.next();
            CandleStick aggregateCandle = aggregatesPerSummaryBeat.get(summaryBeat).getAggregateCandleStick();

            MetricCell cell = new MetricCell(summaryBeat, metricRowKey, aggregateType, aggregateCandle);
            row.add(cell);
        }
        return row;
    }


}
