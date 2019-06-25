package com.dreamscale.htmflow.core.gridtime.kernel.memory.grid.track;

import com.dreamscale.htmflow.core.gridtime.kernel.clock.MusicClock;
import com.dreamscale.htmflow.core.gridtime.kernel.clock.RelativeBeat;
import com.dreamscale.htmflow.core.gridtime.kernel.commons.DefaultCollections;
import com.dreamscale.htmflow.core.gridtime.kernel.memory.grid.cell.*;

import java.util.Iterator;
import java.util.LinkedHashMap;

public class MetricsTrack {
    private final MusicClock musicClock;

    private LinkedHashMap<RelativeBeat, GridMetrics> metricsPerBeat = DefaultCollections.map();

    public MetricsTrack(MusicClock musicClock) {
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

    public GridRow toGridRow(MetricType metricType, AggregateType aggregateType) {
        Iterator<RelativeBeat> iterator = musicClock.getForwardsIterator();

        GridRow row = new GridRow(metricType.toDisplayString());

        while (iterator.hasNext()) {
            RelativeBeat beat = iterator.next();
            CandleStick candleStick = getCandleStick(metricsPerBeat.get(beat), metricType);
            MetricCell cell = new MetricCell(beat, metricType, aggregateType, candleStick);
            row.add(cell);
        }

        return row;
    }

    private CandleStick getCandleStick(GridMetrics metrics, MetricType metricType) {
        CandleStick candleStick = null;
        if (metrics != null) {
            candleStick = metrics.getMetric(metricType);
        }
        return candleStick;
    }
}
