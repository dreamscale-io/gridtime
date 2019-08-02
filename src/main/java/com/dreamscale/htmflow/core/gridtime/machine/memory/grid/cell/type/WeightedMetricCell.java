package com.dreamscale.htmflow.core.gridtime.machine.memory.grid.cell.type;

import com.dreamscale.htmflow.core.gridtime.machine.clock.RelativeBeat;
import com.dreamscale.htmflow.core.gridtime.machine.memory.feature.reference.FeatureReference;
import com.dreamscale.htmflow.core.gridtime.machine.memory.grid.cell.CellSize;
import com.dreamscale.htmflow.core.gridtime.machine.memory.grid.cell.metrics.AggregateType;
import com.dreamscale.htmflow.core.gridtime.machine.memory.grid.cell.metrics.CandleStick;
import com.dreamscale.htmflow.core.gridtime.machine.memory.grid.cell.metrics.WeightedMetric;
import com.dreamscale.htmflow.core.gridtime.machine.memory.grid.query.key.MetricRowKey;
import com.dreamscale.htmflow.core.gridtime.machine.memory.tag.FeatureTag;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.UUID;

@Getter
public class WeightedMetricCell implements GridCell {

    private static final String EMPTY_CELL = "";

    private static final String TRUNCATED_INDICATOR = "*";
    private final int defaultCellSizeForBeat;
    private final WeightedMetric weightedMetric;
    private final String headerColumn;

    private MetricRowKey metricRowKey;

    public WeightedMetricCell(int summaryColumnCount, RelativeBeat beat, MetricRowKey metricRowKey, WeightedMetric weightedMetric) {
        this.headerColumn = beat.toDisplayString();
        this.metricRowKey = metricRowKey;
        this.weightedMetric = weightedMetric;

        this.defaultCellSizeForBeat = CellSize.calculateCellSizeWithSummaryCell(beat, summaryColumnCount);
    }

    public WeightedMetricCell(String headerColumn, MetricRowKey metricRowKey, WeightedMetric weightedMetric) {
        this.headerColumn = headerColumn;
        this.metricRowKey = metricRowKey;
        this.weightedMetric = weightedMetric;

        this.defaultCellSizeForBeat = CellSize.calculateFixedCellSize();
    }


    public String toHeaderCell() {
        return toHeaderCell(defaultCellSizeForBeat);
    }

    public String toHeaderCell(int overrideCellSize) {
        return toRightSizedCell(headerColumn, overrideCellSize);
    }

    public String toValueCell() {
        return toValueCell(defaultCellSizeForBeat);
    }

    public String toValueCell(int overrideCellSize) {
        return toRightSizedCell(toDisplayString(), overrideCellSize);
    }

    @Override
    public Object toValue() {
        return weightedMetric;
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
        if (weightedMetric != null) {
            double value = weightedMetric.getMetric();
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
