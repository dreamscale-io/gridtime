package com.dreamscale.htmflow.core.gridtime.kernel.memory.grid.cell;

import com.dreamscale.htmflow.core.gridtime.kernel.commons.DefaultCollections;
import com.dreamscale.htmflow.core.gridtime.kernel.memory.tag.FeatureTag;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

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

    public List<FeatureTag<?>> getFeatureTags() {
        List<FeatureTag<?>> featureTags = new ArrayList<>();
        for (GridCell cell : gridCells) {
            List<FeatureTag<?>> cellTags = cell.getFeatureTags();

            if (cellTags != null) {
                featureTags.addAll(cellTags);
            }
        }

        return featureTags;
    }

    public Map<String, CellValue> toCellValueMap() {
        Map<String, CellValue> cellValueMap = new LinkedHashMap<>();
        for (GridCell cell : gridCells) {
            String beat = cell.toHeaderCell().trim();
            String value = cell.toValueCell().trim();

            List<UUID> refs = cell.getFeatureRefs();

            if (value.length() > 0) {
                cellValueMap.put(beat, new CellValue(value, refs));
            }
        }
        return cellValueMap;
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
