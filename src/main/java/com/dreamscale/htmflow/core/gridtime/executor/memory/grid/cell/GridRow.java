package com.dreamscale.htmflow.core.gridtime.executor.memory.grid.cell;

import com.dreamscale.htmflow.core.gridtime.executor.machine.parts.commons.DefaultCollections;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Getter
public class GridRow {

    private String rowName;
    private static final int DEFAULT_ROW_NAME_COLUMN_SIZE = 14;


    List<GridCell> gridCells = DefaultCollections.list();

    public GridRow(String rowName) {
        this.rowName = rowName;
    }

    public void add(GridCell cell) {
        gridCells.add(cell);
    }

    public List<String> toHeaderColumns() {
        List<String> headerColumns = new ArrayList<>();
        headerColumns.add(toRightSizedRowName(""));
        for (GridCell cell : gridCells) {
            headerColumns.add(cell.toHeaderCell());
        }
        return headerColumns;
    }

    public List<String> toValueRow() {
        List<String> valueColumns = new ArrayList<>();
        valueColumns.add(toRightSizedRowName(rowName));
        for (GridCell cell : gridCells) {
            valueColumns.add(cell.toValueCell());
        }

        return valueColumns;
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
