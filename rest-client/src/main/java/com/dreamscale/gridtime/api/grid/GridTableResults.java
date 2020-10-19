package com.dreamscale.gridtime.api.grid;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GridTableResults implements Results {

    String title;
    List<String> headers;
    List<List<String>> rowsOfPaddedCells;


    public String getCell(String rowKey, String columnKey) {
        int columnIndex = findColumnIndex(columnKey);
        int rowIndex = findRowIndex(rowKey);

        return rowsOfPaddedCells.get(rowIndex).get(columnIndex).trim();
    }

    public String getQuarterCell(String rowKey, String columnKey) {
        int columnIndex = findQuarterColumnIndex(columnKey);
        int rowIndex = findRowIndex(rowKey);

        return rowsOfPaddedCells.get(rowIndex).get(columnIndex).trim();
    }

    private int findRowIndex(String rowKey) {
        for (int i = 0; i < rowsOfPaddedCells.size(); i++) {
            String rowKeyColumn = rowsOfPaddedCells.get(i).get(0);
            if (rowKeyColumn.trim().equals(rowKey)) {
                return i;
            }
        }
        return 0;
    }

    private int findQuarterColumnIndex(String columnKey) {
        int normalColumnIndex = findColumnIndex(columnKey);

        return (normalColumnIndex - 1) / 5 + 1;
    }

    private int findColumnIndex(String columnKey) {
        for (int i = 0; i < headers.size(); i++) {
            if (headers.get(i).trim().equals(columnKey)) {
                return i;
            }
        }
        return 0;
    }

    @Override
    public String toDisplayString() {
        String gridOutput = "\n";

        gridOutput += "|" + title + "\n";

        gridOutput += "|" + StringUtils.join(headers, "|") + "\n";

        for (List<String> row : rowsOfPaddedCells) {
            gridOutput += "|"+ StringUtils.join(row, "|") + "\n";
        }

        return gridOutput;
    }

    @Override
    public String toString() {
        return toDisplayString();
    }
}
