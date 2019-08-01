package com.dreamscale.htmflow.core.gridtime.machine.memory.grid.cell.type;

import com.dreamscale.htmflow.core.gridtime.machine.clock.RelativeBeat;
import com.dreamscale.htmflow.core.gridtime.machine.memory.feature.reference.FeatureReference;
import com.dreamscale.htmflow.core.gridtime.machine.memory.grid.cell.CellSize;
import com.dreamscale.htmflow.core.gridtime.machine.memory.grid.cell.metrics.AggregateType;
import com.dreamscale.htmflow.core.gridtime.machine.memory.grid.cell.metrics.CandleStick;
import com.dreamscale.htmflow.core.gridtime.machine.memory.grid.cell.metrics.MetricDistribution;
import com.dreamscale.htmflow.core.gridtime.machine.memory.grid.query.key.MetricRowKey;
import com.dreamscale.htmflow.core.gridtime.machine.memory.tag.FeatureTag;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.UUID;

@Getter
public class MetricCell implements GridCell {


    private static final String EMPTY_CELL = "";

    private static final String TRUNCATED_INDICATOR = "*";
    private final AggregateType aggregateType;
    private final int cellSizeForBeat;
    private final String headerStr;

    private MetricRowKey metricRowKey;
    private MetricDistribution metricDistribution;

    public MetricCell(RelativeBeat beat, MetricRowKey metricRowKey, AggregateType aggregateType, MetricDistribution metricDistribution) {
        this.headerStr = beat.toDisplayString();
        this.metricRowKey = metricRowKey;
        this.aggregateType = aggregateType;
        this.metricDistribution = metricDistribution;

        this.cellSizeForBeat = CellSize.calculateCellSize(beat);
    }

    public MetricCell(MetricRowKey metricRowKey, AggregateType aggregateType, MetricDistribution metricDistribution) {
        this.headerStr = aggregateType.getHeader();
        this.metricRowKey = metricRowKey;
        this.aggregateType = aggregateType;
        this.metricDistribution = metricDistribution;

        this.cellSizeForBeat = CellSize.calculateSummaryCellSize();
    }


    public String toHeaderCell() {
        return toHeaderCell(cellSizeForBeat);
    }

    public String toHeaderCell(int overrideCellSize) {
        return toRightSizedCell(headerStr, overrideCellSize);
    }

    public String toValueCell() {
        return toValueCell(cellSizeForBeat);
    }

    public String toValueCell(int overrideCellSize) {
        return toRightSizedCell(toDisplayString(), overrideCellSize);
    }

    @Override
    public Object toValue() {
        return metricDistribution.getValueByAggregateType(aggregateType);
    }

    @Override
    public List<UUID> getFeatureRefs() {
        return null;
    }

    @Override
    public List<FeatureTag<?>> getFeatureTags() {
        return null;
    }

    @Override
    public boolean hasFeature(FeatureReference reference) {
        return false;
    }

    @Override
    public <F extends FeatureReference> F getFeature() {
        return null;
    }


    @Override
    public String toDisplayString() {
        String str;
        if (metricDistribution != null) {
            double value = metricDistribution.getValueByAggregateType(aggregateType);
            str = Double.toString(Math.round(value * 100)*1.0 / 100);
        } else {
            str = EMPTY_CELL;
        }
        return str;
    }

    public String toString() {
        return toValueCell();
    }

    private String toRightSizedCell(String cellContents, int cellSize) {
        String fittedContent;
        if (cellContents.length() > cellSize ) {
            fittedContent = cellContents.substring(0, cellSize - 1) + TRUNCATED_INDICATOR;
        } else {
            fittedContent = StringUtils.rightPad(cellContents, cellSize);
        }
        return fittedContent;
    }



}
