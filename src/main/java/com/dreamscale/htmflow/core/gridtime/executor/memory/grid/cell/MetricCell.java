package com.dreamscale.htmflow.core.gridtime.executor.memory.grid.cell;

import com.dreamscale.htmflow.core.gridtime.executor.clock.RelativeBeat;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

@Getter
public class MetricCell implements GridCell {
    private static final int DEFAULT_CELL_SIZE_FOR_20_BEATS = 8;
    private static final int DEFAULT_CELL_SIZE_FOR_4_BEATS = 40;

    private static final String EMPTY_CELL = "  ";

    private static final String TRUNCATED_INDICATOR = "*";
    private final AggregateType aggregateType;
    private final int cellSizeForBeat;

    private RelativeBeat beat;
    private MetricType metricType;
    private CandleStick candleStick;

    public MetricCell(RelativeBeat beat, MetricType metricType, AggregateType aggregateType, CandleStick candleStick) {
        this.beat = beat;
        this.metricType = metricType;
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


    public String getHeaderCell() {
        return getHeaderCell(cellSizeForBeat);
    }

    public String getHeaderCell(int overrideCellSize) {
        return toRightSizedCell("|"+beat.toShortString(), overrideCellSize);
    }

    public String getValueCell() {
        return getValueCell(cellSizeForBeat);
    }

    public String getValueCell(int overrideCellSize) {
        String str = "|";

        if (candleStick != null) {
            double value = candleStick.getValueByAggregateType(aggregateType);
            str += Math.round(value * 100)*1.0 / 100;
        } else {
            str += EMPTY_CELL;
        }

        return toRightSizedCell(str, overrideCellSize);
    }

    public String toString() {
        return getValueCell();
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
