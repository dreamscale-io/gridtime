package com.dreamscale.htmflow.core.gridtime.machine.memory.grid.track;

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
import java.util.UUID;

@Slf4j
public class WeightedMetricTeamTrack {

    private final MetricRowKey rowKey;

    private Map<UUID, WeightedMetric> metricsPerTeamMember = DefaultCollections.map();
    private Map<UUID, String> columnHeadersPerTeamMember = DefaultCollections.map();


    private WeightedCandleStick summaryCandle = new WeightedCandleStick();


    public WeightedMetricTeamTrack(MetricRowKey rowKey) {
        this.rowKey = rowKey;
    }

    public void addWeightedMetric(UUID torchieId, String columnHeader, Duration durationWeight, Double metric) {
        if (torchieId != null) {
            if (metricsPerTeamMember.get(torchieId) != null) {
                log.warn("Replacing metric at id :"+torchieId +" for "+rowKey.toDisplayString());
            }

            WeightedMetric weightedMetric = new WeightedMetric(durationWeight, metric);
            metricsPerTeamMember.put(torchieId, weightedMetric);
            summaryCandle.addWeightedMetricSample(weightedMetric);

            columnHeadersPerTeamMember.put(torchieId, columnHeader);

        } else {
            log.warn("Null beat for metric "+rowKey.toDisplayString() + ", value: "+metric);
        }
    }

    public void finish() {

    }

    public GridRow toGridRow() {
        GridRow gridRow = new GridRow(rowKey);

        gridRow.addSummaryCell(new MetricCell(rowKey, AggregateType.AVG, summaryCandle));
        gridRow.addSummaryCell(new MetricCell(rowKey, AggregateType.MIN, summaryCandle));
        gridRow.addSummaryCell(new MetricCell(rowKey, AggregateType.MAX, summaryCandle));
        gridRow.addSummaryCell(new MetricCell(rowKey, AggregateType.STDDEV, summaryCandle));
        gridRow.addSummaryCell(new MetricCell(rowKey, AggregateType.TOTAL, summaryCandle));


        Iterator<UUID> orderedKeys = metricsPerTeamMember.keySet().iterator();

        while (orderedKeys.hasNext()) {
            UUID torchieId = orderedKeys.next();
            String columnHeader = columnHeadersPerTeamMember.get(torchieId);
            WeightedMetric weightedMetric = metricsPerTeamMember.get(torchieId);

            gridRow.add(new WeightedMetricCell(columnHeader, rowKey, weightedMetric));
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
