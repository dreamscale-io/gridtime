package com.dreamscale.gridtime.core.machine.executor.monitor;

import com.dreamscale.gridtime.core.machine.executor.circuit.CircuitMonitor;
import com.dreamscale.gridtime.core.machine.memory.grid.cell.CellFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class CircuitActivitySummary {

    private int numberWorkers;

    private int ticksProcessed;

    private LocalDateTime lastUpdated;

    private double executionTimeAvg;
    private double executionTimeMax;

    private double queueTimeAvg;
    private double queueTimeMax;

    private int queueDepth;

    public void aggregateMonitor(CircuitMonitor circuitMonitor) {

        ticksProcessed += circuitMonitor.getTicksProcessed();

        lastUpdated = maxDate(lastUpdated, circuitMonitor.getLastStatusUpdate());

        executionTimeAvg = ((numberWorkers * executionTimeAvg ) + circuitMonitor.getExecutionTimeMetric().getAvg()) / (numberWorkers + 1);
        executionTimeMax = Math.max(executionTimeMax, circuitMonitor.getExecutionTimeMetric().getMax());

        queueTimeAvg = ((numberWorkers * queueTimeAvg ) + circuitMonitor.getExecutionTimeMetric().getAvg()) / (numberWorkers + 1);
        queueTimeMax = Math.max(queueTimeMax, circuitMonitor.getQueueTimeMetric().getMax());

        queueDepth += circuitMonitor.getQueueDepth();

        numberWorkers++;
    }

    public void aggregate(CircuitActivitySummary activitySummary) {
        ticksProcessed += activitySummary.getTicksProcessed();

        lastUpdated = maxDate(lastUpdated, activitySummary.getLastUpdated());

        executionTimeAvg = ((numberWorkers * executionTimeAvg ) +
                (activitySummary.getNumberWorkers() * activitySummary.getExecutionTimeAvg()) / (numberWorkers + activitySummary.getNumberWorkers()));

        executionTimeMax = Math.max(executionTimeMax, activitySummary.getExecutionTimeMax());

        queueTimeAvg = ((numberWorkers * queueTimeAvg ) +
                (activitySummary.getNumberWorkers() * activitySummary.getQueueTimeAvg()) / (numberWorkers + activitySummary.getNumberWorkers()));

        queueTimeMax = Math.max(queueTimeMax, activitySummary.getQueueTimeMax());

        queueDepth += activitySummary.getQueueDepth();

        numberWorkers += activitySummary.getNumberWorkers();

    }

    List<String> toRow(String rowKey) {
        List<String> row = new ArrayList<>();

        row.add(CellFormat.toRightSizedCell(rowKey, 10));
        row.add(CellFormat.toCell(numberWorkers, 7));
        row.add(CellFormat.toCell(ticksProcessed, 5));
        row.add(CellFormat.toCell(lastUpdated, 10));
        row.add(CellFormat.toCell(executionTimeAvg, 7));
        row.add(CellFormat.toCell(executionTimeMax, 7));
        row.add(CellFormat.toCell(queueTimeAvg, 5));
        row.add(CellFormat.toCell(queueTimeMax, 5));
        row.add(CellFormat.toCell(queueDepth, 5));

        return row;
    }

    List<String> toHeaderRow() {
        List<String> row = new ArrayList<>();

        row.add(CellFormat.toRightSizedCell("", 10));
        row.add(CellFormat.toRightSizedCell("Workers", 7));
        row.add(CellFormat.toRightSizedCell("Ticks", 5));
        row.add(CellFormat.toRightSizedCell("LastUpdate", 10));
        row.add(CellFormat.toRightSizedCell("ExecAvg", 7));
        row.add(CellFormat.toRightSizedCell("ExecMax", 7));
        row.add(CellFormat.toRightSizedCell("QAvg", 5));
        row.add(CellFormat.toRightSizedCell("QMax", 5));
        row.add(CellFormat.toRightSizedCell("QDepth", 5));

        return row;
    }


    public CircuitActivitySummary clone() {
        try {
            return (CircuitActivitySummary) super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return null;
    }


    private LocalDateTime maxDate(LocalDateTime date1, LocalDateTime date2) {
        if (date1 == null) {
            return date2;
        }
        if (date2 == null) {
            return date1;
        }

        if (date1.isAfter(date2)) {
            return date1;
        } else {
            return date2;
        }
    }


}
