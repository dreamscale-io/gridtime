package com.dreamscale.htmflow.core.gridtime.kernel.memory.grid.cell.type;

import com.dreamscale.htmflow.core.gridtime.kernel.clock.RelativeBeat;
import com.dreamscale.htmflow.core.gridtime.kernel.memory.feature.reference.FeatureReference;
import com.dreamscale.htmflow.core.gridtime.kernel.memory.grid.cell.metrics.AggregateType;
import com.dreamscale.htmflow.core.gridtime.kernel.memory.grid.cell.metrics.CandleStick;
import com.dreamscale.htmflow.core.gridtime.kernel.memory.grid.query.key.MetricRowKey;
import com.dreamscale.htmflow.core.gridtime.kernel.memory.tag.FeatureTag;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.UUID;

@Getter
public class MetricCell implements GridCell {
    private static final int DEFAULT_CELL_SIZE_FOR_20_BEATS = 7;
    private static final int DEFAULT_CELL_SIZE_FOR_4_BEATS = 39;

    private static final String EMPTY_CELL = "";

    private static final String TRUNCATED_INDICATOR = "*";
    private final AggregateType aggregateType;
    private final int cellSizeForBeat;

    private RelativeBeat beat;
    private MetricRowKey metricRowKey;
    private CandleStick candleStick;

    public MetricCell(RelativeBeat beat, MetricRowKey metricRowKey, AggregateType aggregateType, CandleStick candleStick) {
        this.beat = beat;
        this.metricRowKey = metricRowKey;
        this.aggregateType = aggregateType;
        this.candleStick = candleStick;

        this.cellSizeForBeat = calculateCellSize(beat);
    }

    private int calculateCellSize(RelativeBeat beat) {
        if (beat.getBeatsPerMeasure() == 20) {
            return DEFAULT_CELL_SIZE_FOR_20_BEATS;
        } else {
            return DEFAULT_CELL_SIZE_FOR_4_BEATS;
        }
    }

    public String toHeaderCell() {
        return toHeaderCell(cellSizeForBeat);
    }

    public String toHeaderCell(int overrideCellSize) {
        return toRightSizedCell(beat.toDisplayString(), overrideCellSize);
    }

    public String toValueCell() {
        return toValueCell(cellSizeForBeat);
    }

    public String toValueCell(int overrideCellSize) {
        return toRightSizedCell(toDisplayString(), overrideCellSize);
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
        if (candleStick != null) {
            double value = candleStick.getValueByAggregateType(aggregateType);
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
