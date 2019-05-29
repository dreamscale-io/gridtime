package com.dreamscale.htmflow.core.gridtime.executor.memory.grid.cell;

import com.dreamscale.htmflow.core.gridtime.executor.clock.RelativeBeat;
import com.dreamscale.htmflow.core.gridtime.executor.machine.capabilities.cmd.tag.Tag;
import com.dreamscale.htmflow.core.gridtime.executor.machine.parts.commons.DefaultCollections;
import com.dreamscale.htmflow.core.gridtime.executor.memory.feature.reference.FeatureReference;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

@Getter
public class GridRow {

    private String rowName;
    private static final int DEFAULT_ROW_NAME_COLUMN_SIZE = 15;


    List<GridCell> gridCells = DefaultCollections.list();

    public GridRow(String rowName) {
        this.rowName = rowName;
    }

    public void add(GridCell cell) {
        gridCells.add(cell);
    }

    public String getPrintedHeaderRow() {
        String headerRow = toRightSizedRowName("|");
        for (GridCell cell : gridCells) {
            headerRow += cell.getHeaderCell();
        }

        return headerRow;
    }

    public String getPrintedValueRow() {
        String valueRow = toRightSizedRowName("|"+rowName);
        for (GridCell cell : gridCells) {
            valueRow += cell.getValueCell();
        }

        return valueRow;
    }

    private String toRightSizedRowName(String rowName) {
        String fittedContent;
        if ( rowName.length() > DEFAULT_ROW_NAME_COLUMN_SIZE ) {
            fittedContent = rowName.substring(0, DEFAULT_ROW_NAME_COLUMN_SIZE);
        } else {
            fittedContent = StringUtils.rightPad(rowName, DEFAULT_ROW_NAME_COLUMN_SIZE);
        }
        return fittedContent;
    }
}
