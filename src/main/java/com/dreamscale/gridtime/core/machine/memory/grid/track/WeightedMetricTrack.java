package com.dreamscale.gridtime.core.machine.memory.grid.track;

import com.dreamscale.gridtime.core.machine.clock.MusicClock;
import com.dreamscale.gridtime.core.machine.clock.RelativeBeat;
import com.dreamscale.gridtime.core.machine.commons.DefaultCollections;
import com.dreamscale.gridtime.core.machine.memory.grid.cell.GridRow;
import com.dreamscale.gridtime.core.machine.memory.grid.cell.metrics.AggregateType;
import com.dreamscale.gridtime.core.machine.memory.grid.cell.metrics.WeightedCandleStick;
import com.dreamscale.gridtime.core.machine.memory.grid.cell.metrics.WeightedMetric;
import com.dreamscale.gridtime.core.machine.memory.grid.cell.type.MetricCell;
import com.dreamscale.gridtime.core.machine.memory.grid.cell.type.WeightedMetricCell;
import com.dreamscale.gridtime.core.machine.memory.grid.query.key.MetricRowKey;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

@Slf4j
public class WeightedMetricTrack {

    private final MetricRowKey rowKey;
    private final MusicClock musicClock;

    private Map<RelativeBeat, WeightedMetric> metricsPerBeat = DefaultCollections.map();
    private WeightedCandleStick summaryCandle = new WeightedCandleStick();


    public WeightedMetricTrack(MetricRowKey rowKey, MusicClock musicClock) {
        this.rowKey = rowKey;
        this.musicClock = musicClock;
    }

    public void addWeightedMetric(RelativeBeat beat, Duration durationWeight, Double metric) {
        if (beat != null) {
            if (metricsPerBeat.get(beat) != null) {
                log.warn("Replacing metric at beat :"+beat.toDisplayString() +" for "+rowKey.toDisplayString());
            }

            WeightedMetric weightedMetric = new WeightedMetric(durationWeight, metric);
            metricsPerBeat.put(beat, weightedMetric);
            summaryCandle.addWeightedMetricSample(weightedMetric);

        } else {
            log.warn("Null beat for metric "+rowKey.toDisplayString() + ", value: "+metric);
        }
    }

    public void finish() {

    }

    public GridRow toGridRow() {
        GridRow gridRow = new GridRow(rowKey);

        Iterator<RelativeBeat> beatIterator = musicClock.getForwardsIterator();

        gridRow.addSummaryCell(new MetricCell(rowKey, AggregateType.AVG, summaryCandle));
        gridRow.addSummaryCell(new MetricCell(rowKey, AggregateType.MIN, summaryCandle));
        gridRow.addSummaryCell(new MetricCell(rowKey, AggregateType.MAX, summaryCandle));
        gridRow.addSummaryCell(new MetricCell(rowKey, AggregateType.STDDEV, summaryCandle));
        gridRow.addSummaryCell(new MetricCell(rowKey, AggregateType.TOTAL, summaryCandle));


        while (beatIterator.hasNext()) {
            RelativeBeat beat = beatIterator.next();

            gridRow.add(new WeightedMetricCell(3, beat, rowKey, metricsPerBeat.get(beat)));
        }
        return gridRow;
    }


    public Collection<? extends GridRow> toGridRows() {
        return DefaultCollections.toList(toGridRow());
    }


    public Double getTotalCalculation() {
        return summaryCandle.getTotal();
    }
}
