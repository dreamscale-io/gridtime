package com.dreamscale.gridtime.core.machine.executor.dashboard;

import com.dreamscale.gridtime.core.machine.clock.GeometryClock;
import com.dreamscale.gridtime.core.machine.executor.circuit.CircuitMonitor;
import com.dreamscale.gridtime.core.machine.memory.grid.cell.CellFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class CircuitActivitySummaryRow implements Cloneable {

    private int numberWorkers;

    private int ticksProcessed;

    private GeometryClock.GridTime lastGridtime;

    private double executionTimeMin;
    private double executionTimeAvg;
    private double executionTimeMax;

    private double queueTimeMin;
    private double queueTimeAvg;
    private double queueTimeMax;


    public void aggregateMonitor(CircuitMonitor circuitMonitor) {

        ticksProcessed += circuitMonitor.getTicksProcessed();

        lastGridtime = maxGridtime(lastGridtime, circuitMonitor.getLastGridtime());

        executionTimeMin = Math.min(executionTimeMin, circuitMonitor.getRecentExecutionTimeMetric().getMin());
        executionTimeAvg = ((numberWorkers * executionTimeAvg) + circuitMonitor.getRecentExecutionTimeMetric().getAvg()) / (numberWorkers + 1);
        executionTimeMax = Math.max(executionTimeMax, circuitMonitor.getRecentExecutionTimeMetric().getMax());

        queueTimeMin = Math.max(queueTimeMax, circuitMonitor.getRecentQueueTimeMetric().getMin());
        queueTimeAvg = ((numberWorkers * queueTimeAvg) + circuitMonitor.getRecentQueueTimeMetric().getAvg()) / (numberWorkers + 1);
        queueTimeMax = Math.max(queueTimeMax, circuitMonitor.getRecentQueueTimeMetric().getMax());

        numberWorkers++;
    }

    public void aggregate(CircuitActivitySummaryRow activitySummary) {
        ticksProcessed += activitySummary.getTicksProcessed();

        lastGridtime = maxGridtime(lastGridtime, activitySummary.getLastGridtime());

        executionTimeMin = Math.max(executionTimeMin, activitySummary.getExecutionTimeMax());
        executionTimeAvg = ((numberWorkers * executionTimeAvg) +
                (activitySummary.getNumberWorkers() * activitySummary.getExecutionTimeAvg()) / (numberWorkers + activitySummary.getNumberWorkers()));

        executionTimeMax = Math.max(executionTimeMax, activitySummary.getExecutionTimeMax());

        queueTimeMin = Math.max(queueTimeMax, activitySummary.getQueueTimeMin());
        queueTimeAvg = ((numberWorkers * queueTimeAvg) +
                (activitySummary.getNumberWorkers() * activitySummary.getQueueTimeAvg()) / (numberWorkers + activitySummary.getNumberWorkers()));

        queueTimeMax = Math.max(queueTimeMax, activitySummary.getQueueTimeMax());

        numberWorkers += activitySummary.getNumberWorkers();

    }

    List<String> toRow(String rowKey) {
        List<String> row = new ArrayList<>();

        row.add(CellFormat.toRightSizedCell(rowKey, 10));
        row.add(CellFormat.toCell(numberWorkers, 8));
        row.add(CellFormat.toCell(ticksProcessed, 6));
        row.add(CellFormat.toCell(lastGridtime, 24));
        row.add(CellFormat.toCell(executionTimeMin, 8));
        row.add(CellFormat.toCell(executionTimeAvg, 8));
        row.add(CellFormat.toCell(executionTimeMax, 8));
        row.add(CellFormat.toCell(queueTimeMin, 7));
        row.add(CellFormat.toCell(queueTimeAvg, 7));
        row.add(CellFormat.toCell(queueTimeMax, 7));

        return row;
    }

    List<String> toHeaderRow() {
        List<String> row = new ArrayList<>();

        row.add(CellFormat.toRightSizedCell("", 10));
        row.add(CellFormat.toRightSizedCell("Workers", 8));
        row.add(CellFormat.toRightSizedCell("Ticks", 6));
        row.add(CellFormat.toRightSizedCell("LastGridtime", 24));
        row.add(CellFormat.toRightSizedCell("ExecMin", 8));
        row.add(CellFormat.toRightSizedCell("ExecAvg", 8));
        row.add(CellFormat.toRightSizedCell("ExecMax", 8));
        row.add(CellFormat.toRightSizedCell("QMin", 7));
        row.add(CellFormat.toRightSizedCell("QAvg", 7));
        row.add(CellFormat.toRightSizedCell("QMax", 7));

        return row;
    }


    public CircuitActivitySummaryRow clone() {
        try {
            return (CircuitActivitySummaryRow) super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return null;
    }


    private GeometryClock.GridTime maxGridtime(GeometryClock.GridTime gridtime1, GeometryClock.GridTime gridtime2) {
        if (gridtime1 == null) {
            return gridtime2;
        }
        if (gridtime2 == null) {
            return gridtime1;
        }

        if (gridtime1.isAfter(gridtime2)) {
            return gridtime1;
        } else {
            return gridtime2;
        }
    }


}
