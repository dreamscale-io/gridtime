package com.dreamscale.htmflow.core.gridtime.machine.memory.grid.track;

import com.dreamscale.htmflow.core.gridtime.machine.clock.MusicClock;
import com.dreamscale.htmflow.core.gridtime.machine.clock.RelativeBeat;
import com.dreamscale.htmflow.core.gridtime.machine.commons.DefaultCollections;
import com.dreamscale.htmflow.core.gridtime.machine.memory.grid.cell.GridRow;
import com.dreamscale.htmflow.core.gridtime.machine.memory.grid.cell.metrics.AggregateType;
import com.dreamscale.htmflow.core.gridtime.machine.memory.grid.cell.metrics.WeightedCandleStick;
import com.dreamscale.htmflow.core.gridtime.machine.memory.grid.cell.metrics.WeightedMetric;
import com.dreamscale.htmflow.core.gridtime.machine.memory.grid.cell.type.MetricCell;
import com.dreamscale.htmflow.core.gridtime.machine.memory.grid.cell.type.WeightedMetricCell;
import com.dreamscale.htmflow.core.gridtime.machine.memory.grid.query.key.MetricRowKey;
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

        gridRow.add(new MetricCell("Summary", musicClock.getBeat(1), rowKey, AggregateType.AVG, summaryCandle));

        while (beatIterator.hasNext()) {
            RelativeBeat beat = beatIterator.next();

            gridRow.add(new WeightedMetricCell(beat, rowKey, metricsPerBeat.get(beat)));
        }
        return gridRow;
    }


    public Collection<? extends GridRow> toGridRows() {
        return DefaultCollections.toList(toGridRow());
    }
}
