package com.dreamscale.gridtime.core.machine.executor.dashboard;

import com.dreamscale.gridtime.core.machine.clock.GeometryClock;
import com.dreamscale.gridtime.core.machine.executor.circuit.CircuitMonitor;
import com.dreamscale.gridtime.core.machine.memory.grid.cell.CellFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.*;

@Data
public class FailureDetailRow implements Cloneable {

    private final String rowKey;

    private int ticksProcessed;
    private int ticksFailed;

    private final Exception failure;

    FailureDetailRow(CircuitActivitySummaryRow summaryRow, Exception ex) {
        this.rowKey = summaryRow.getRowKey();
        this.ticksProcessed = summaryRow.getTicksProcessed();
        this.ticksFailed = summaryRow.getTicksFailed();
        this.failure = ex;
    }

    List<List<String>> toRows() {
        List<List<String>> rows = new ArrayList<>();

        String firstLine = failure.getClass().getSimpleName() + " : " + failure.getMessage();
        String stack1 = failure.getStackTrace()[0].toString();
        String stack2 = failure.getStackTrace()[1].toString();

        List<String> row = new ArrayList<>();
        row.add(CellFormat.toRightSizedCell(rowKey, 18));
        row.add(CellFormat.toCell(ticksProcessed, 6));
        row.add(CellFormat.toCell(ticksFailed, 6));
        row.add(firstLine);

        List<String> row2 = new ArrayList<>();
        row2.add(CellFormat.toRightSizedCell("", 32));
        row2.add(stack1);

        List<String> row3 = new ArrayList<>();
        row3.add(CellFormat.toRightSizedCell("", 32));
        row3.add(stack2);

        rows.add(row);
        rows.add(row2);
        rows.add(row3);

        return rows;
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
