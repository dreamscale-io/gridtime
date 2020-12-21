package com.dreamscale.gridtime.core.machine.memory.grid.cell;

import com.dreamscale.gridtime.core.machine.clock.RelativeBeat;
import com.dreamscale.gridtime.core.machine.commons.DefaultCollections;
import com.dreamscale.gridtime.core.machine.memory.grid.cell.metrics.AggregateType;
import com.dreamscale.gridtime.core.machine.memory.grid.cell.type.GridCell;
import com.dreamscale.gridtime.core.machine.memory.grid.query.key.Key;
import com.dreamscale.gridtime.core.machine.memory.tag.FeatureTag;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

@Getter
public class GridRow implements Iterable<GridCell> {

    private Key rowKey;

    List<GridCell> gridCells = DefaultCollections.list();

    int summaryCellCount = 0;

    public GridRow(Key rowKey) {
        this.rowKey = rowKey;
    }

    public void add(GridCell cell) {
        gridCells.add(cell);
    }

    public void addSummaryCell(GridCell summaryCell) {
        gridCells.add(summaryCell);
        summaryCellCount++;
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
        valueColumns.add(toRightSizedRowName(rowKey.getName()));
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

    public CellValueMap toCellValueMap() {
        CellValueMap cellValueMap = new CellValueMap();
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
        int colSize = CellSize.calculateRowKeyCellSize();
        if ( rowName.length() > colSize ) {
            fittedContent = rowName.substring(0, colSize);
        } else {
            fittedContent = StringUtils.rightPad(rowName, colSize);
        }
        return fittedContent;
    }


    @Override
    public Iterator<GridCell> iterator() {
        return gridCells.iterator();
    }

    public GridCell getSummaryCell(AggregateType aggregateType) {
        return getSummaryCell(aggregateType.getHeader());
    }

    public GridCell getSummaryCell(String header) {
        for (GridCell cell : gridCells) {
            if (cell.toHeaderCell().trim().equals(header)) {
                return cell;
            }
        }
        return null;
    }

    public GridCell getCell(RelativeBeat beat) {
        return gridCells.get(beat.getBeat() + summaryCellCount - 1);
    }

    public GridCell getLast() {
        return gridCells.get(gridCells.size() - 1);
    }


}
