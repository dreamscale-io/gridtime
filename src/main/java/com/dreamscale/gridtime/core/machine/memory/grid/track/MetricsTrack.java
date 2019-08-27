package com.dreamscale.gridtime.core.machine.memory.grid.track;

import com.dreamscale.gridtime.core.machine.clock.GeometryClock;
import com.dreamscale.gridtime.core.machine.clock.MusicClock;
import com.dreamscale.gridtime.core.machine.clock.RelativeBeat;
import com.dreamscale.gridtime.core.machine.commons.DefaultCollections;
import com.dreamscale.gridtime.core.machine.memory.grid.cell.*;
import com.dreamscale.gridtime.core.machine.memory.grid.cell.metrics.AggregateType;
import com.dreamscale.gridtime.core.machine.memory.grid.cell.metrics.CandleStick;
import com.dreamscale.gridtime.core.machine.memory.grid.cell.metrics.GridMetrics;
import com.dreamscale.gridtime.core.machine.memory.grid.query.key.MetricRowKey;
import com.dreamscale.gridtime.core.machine.memory.grid.cell.type.MetricCell;

import java.util.Iterator;
import java.util.LinkedHashMap;

public class MetricsTrack {
    private final MusicClock musicClock;
    private final GeometryClock.GridTime gridTime;

    private LinkedHashMap<RelativeBeat, GridMetrics> metricsPerBeat = DefaultCollections.map();

    public MetricsTrack(GeometryClock.GridTime gridTime, MusicClock musicClock) {
        this.gridTime = gridTime;
        this.musicClock = musicClock;
    }

    public GridMetrics getMetricsFor(RelativeBeat beat) {
        RelativeBeat summaryBeat = beat.toSummaryBeat();

        GridMetrics metrics = metricsPerBeat.get(beat);
        GridMetrics summaryMetrics = metricsPerBeat.get(summaryBeat);

        if (summaryMetrics == null) {
            summaryMetrics = new GridMetrics();
            metricsPerBeat.put(summaryBeat, summaryMetrics);
        }

        if (metrics == null) {
            metrics = new GridMetrics(summaryMetrics);
            metricsPerBeat.put(beat, metrics);
        }
        return metrics;
    }

    public GridRow toGridRow(MetricRowKey metricRowKey, AggregateType aggregateType) {
        Iterator<RelativeBeat> iterator = musicClock.getForwardsIterator();

        GridRow row = new GridRow(metricRowKey);

        while (iterator.hasNext()) {
            RelativeBeat beat = iterator.next();
            CandleStick candleStick = getCandleStick(metricsPerBeat.get(beat), metricRowKey);
            MetricCell cell = new MetricCell(beat, metricRowKey, aggregateType, candleStick);
            row.add(cell);
        }

        return row;
    }

    private CandleStick getCandleStick(GridMetrics metrics, MetricRowKey metricRowKey) {
        CandleStick candleStick = null;
        if (metrics != null) {
            candleStick = metrics.getMetric(metricRowKey);
        }
        return candleStick;
    }
}
