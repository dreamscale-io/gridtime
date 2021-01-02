package com.dreamscale.gridtime.core.machine.memory.grid.cell;

import java.util.ArrayList;
import java.util.List;

public class RightSizer {

    public static void rightSizeCells(List<String> headerRow, List<List<String>> valueRows) {
        List<Integer> colSizes = new ArrayList<>();

        for (int j = 0;  j < headerRow.size(); j++) {

            int maxColSize = headerRow.get(j).length();

            for (int i = 0; i < valueRows.size(); i++) {
                maxColSize = Math.max(maxColSize, valueRows.get(i).get(j).length());
            }

            headerRow.set(j, CellFormat.toRightSizedCell(headerRow.get(j), maxColSize + 1));

            for (int i = 0; i < valueRows.size(); i++) {
                valueRows.get(i).set(j, CellFormat.toRightSizedCell(valueRows.get(i).get(j), maxColSize + 1));
            }

            colSizes.add(maxColSize);
        }
    }

    public static void rightSizeCells(List<String> headerRow, List<List<String>> valueRows, int tableWidth) {
        int remainingWidth = tableWidth;

        for (int j = 0;  j < headerRow.size(); j++) {

            int maxColSize = headerRow.get(j).length();

            for (int i = 0; i < valueRows.size(); i++) {
                maxColSize = Math.max(maxColSize, valueRows.get(i).get(j).length());
            }

            int thisColSize = maxColSize + 1;

            if (j == headerRow.size() - 1) {
                thisColSize = remainingWidth - 1;
            } else {
                remainingWidth -= (thisColSize + 1);
            }

            headerRow.set(j, CellFormat.toRightSizedCell(headerRow.get(j), thisColSize));

            for (int i = 0; i < valueRows.size(); i++) {
                valueRows.get(i).set(j, CellFormat.toRightSizedCell(valueRows.get(i).get(j), thisColSize));
            }

        }
    }

}
