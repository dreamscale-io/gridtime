package com.dreamscale.gridtime.core.machine.executor.dashboard;

import com.dreamscale.gridtime.core.machine.memory.grid.cell.CellFormat;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class FailureDetailRow implements Cloneable {

    private final String rowKey;

    private int ticksProcessed;
    private int ticksFailed;

    private final Exception failure;

    private static final int STACK_FRAMES_TO_SHOW = 5;

    FailureDetailRow(CircuitActivitySummaryRow summaryRow, Exception ex) {
        this.rowKey = summaryRow.getRowKey();
        this.ticksProcessed = summaryRow.getTicksProcessed();
        this.ticksFailed = summaryRow.getTicksFailed();
        this.failure = ex;
    }

    List<List<String>> toRows() {
        List<List<String>> rows = new ArrayList<>();

        String firstLine = failure.getClass().getSimpleName() + " : " + failure.getMessage();

        List<String> firstRow = new ArrayList<>();
        firstRow.add(CellFormat.toRightSizedCell(rowKey, 18));
        firstRow.add(CellFormat.toCell(ticksProcessed, 6));
        firstRow.add(CellFormat.toCell(ticksFailed, 6));
        firstRow.add(firstLine);

        rows.add(firstRow);

        List<String> stackTraceLines = extractStackLines(failure);

        for (String stackLine : stackTraceLines) {
            List<String> stackTraceRow = new ArrayList<>();
            stackTraceRow.add(CellFormat.toRightSizedCell("", 32));
            stackTraceRow.add(stackLine);

            rows.add(stackTraceRow);
        }

        return rows;
    }

    private List<String> extractStackLines(Exception failure) {

        List<String> stackLines = new ArrayList<>();

        StackTraceElement[] stackTraceFrames = failure.getStackTrace();

        for (int i = 0; i < STACK_FRAMES_TO_SHOW && i < stackTraceFrames.length; i++) {
            stackLines.add(stackTraceFrames[i].toString());
        }

        return stackLines;
    }

    List<String> toHeaderRow() {
        List<String> row = new ArrayList<>();

        row.add(CellFormat.toRightSizedCell("ID", 18));
        row.add(CellFormat.toRightSizedCell("Ticks", 6));
        row.add(CellFormat.toRightSizedCell("Fails", 6));
        row.add(CellFormat.toRightSizedCell("Error Message", 80));

        return row;
    }


    public FailureDetailRow clone() {
        try {
            return (FailureDetailRow) super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return null;
    }

}
