package com.dreamscale.gridtime.core.machine.memory.grid.cell;

import com.dreamscale.gridtime.core.machine.clock.GeometryClock;
import com.dreamscale.gridtime.core.machine.clock.RelativeBeat;
import com.dreamscale.gridtime.core.machine.clock.ZoomLevel;
import org.apache.commons.lang3.StringUtils;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class CellFormat {


    private static final String TRUNCATED_INDICATOR = "*";

    public static String toCell(GeometryClock.GridTime value, int cellSize) {
        return toRightSizedCell(toCellValue(value), cellSize);
    }

    public static String toCell(ZoomLevel value, int cellSize) {
        return toRightSizedCell(toCellValue(value), cellSize);
    }

    public static String toCell(UUID value, int cellSize) {
        return toRightSizedCell(toCellValue(value), cellSize);
    }

    public static String toCell(Double value, int cellSize) {
        return toRightSizedCell(toCellValue(value), cellSize);
    }

    public static String toCell(Integer value, int cellSize) {
        return toRightSizedCell(toCellValue(value), cellSize);
    }

    public static String toCell(LocalDateTime value, int cellSize) {
        return toRightSizedCell(toCellValue(value), cellSize);
    }


    public static String toCellValue(ZoomLevel value) {
        return value.name();
    }


    public static String toCellValue(GeometryClock.GridTime value) {
        return value.toDisplayString();
    }

    public static String toCellValue(UUID value) {
        return value.toString().substring(0, 8);
    }


    public static String toCellValue(Double value) {
        return Double.toString(Math.round(value * 100)*1.0 / 100);
    }

    public static String toCellValue(Integer value) {
        return Integer.toString(value);
    }

    public static String toCellValue(LocalDateTime dateTime) {
        return dateTime.format(DateTimeFormatter.ISO_LOCAL_TIME);
    }


    public static String toRightSizedCell(String cellContents, int cellSize) {
        String fittedContent;
        if (cellContents.length() > cellSize ) {
            fittedContent = cellContents.substring(0, cellSize - 1) + TRUNCATED_INDICATOR;
        } else {
            fittedContent = StringUtils.rightPad(cellContents, cellSize);
        }
        return fittedContent;
    }
}
