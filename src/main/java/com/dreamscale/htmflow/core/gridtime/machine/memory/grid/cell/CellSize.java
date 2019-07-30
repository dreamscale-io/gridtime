package com.dreamscale.htmflow.core.gridtime.machine.memory.grid.cell;

import com.dreamscale.htmflow.core.gridtime.machine.clock.RelativeBeat;

public class CellSize {

    private static final int DEFAULT_CELL_SIZE_FOR_20_BEATS = 7;
    private static final int DEFAULT_CELL_SIZE_FOR_12_BEATS = 12;
    private static final int DEFAULT_CELL_SIZE_FOR_6_BEATS = 25;
    private static final int DEFAULT_CELL_SIZE_FOR_4_BEATS = 39;

    public static int calculateCellSizeWithSummaryCell(RelativeBeat beat) {

        int summaryCellSize = calculateCellSize(beat);

        return Math.floorDiv(160 - summaryCellSize + 1 , beat.getBeatsPerMeasure()) - 1;
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
}
