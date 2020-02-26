package com.dreamscale.gridtime.core.machine.memory.grid.cell;

import com.dreamscale.gridtime.core.machine.clock.RelativeBeat;
import org.apache.commons.lang3.StringUtils;

public class CellSize {

    private static final int DEFAULT_CELL_SIZE_FOR_20_BEATS = 7;
    private static final int DEFAULT_CELL_SIZE_FOR_12_BEATS = 12;
    private static final int DEFAULT_CELL_SIZE_FOR_6_BEATS = 25;
    private static final int DEFAULT_CELL_SIZE_FOR_4_BEATS = 39;

    private static final int DEFAULT_CELL_SIZE_FOR_SUMMARY = 10;
    private static final int DEFAULT_CELL_SIZE_FOR_FIXED = 8;

    private static final String TRUNCATED_INDICATOR = "*";


    public static int calculateCellSizeWithSummaryCell(RelativeBeat beat, int numberSummaryColumns) {

        int summaryCellSize = calculateSummaryCellSize() * numberSummaryColumns + numberSummaryColumns;

        return Math.floorDiv(160 - summaryCellSize, beat.getBeatsPerMeasure()) - 1;
    }

    public static int calculateCellSize(RelativeBeat beat) {
        int beatsPerMeasure = beat.getBeatsPerMeasure();
            if (beatsPerMeasure == 20) {
                return DEFAULT_CELL_SIZE_FOR_20_BEATS;
            } else if (beatsPerMeasure == 12) {
                return DEFAULT_CELL_SIZE_FOR_12_BEATS;
            } else if (beatsPerMeasure == 6) {
                return DEFAULT_CELL_SIZE_FOR_6_BEATS;
            } else {
                return DEFAULT_CELL_SIZE_FOR_4_BEATS;
            }
    }

    public static int calculateSummaryCellSize() {
        return DEFAULT_CELL_SIZE_FOR_SUMMARY;
    }

    public static int calculateFixedCellSize() {
        return DEFAULT_CELL_SIZE_FOR_FIXED;
    }


}
