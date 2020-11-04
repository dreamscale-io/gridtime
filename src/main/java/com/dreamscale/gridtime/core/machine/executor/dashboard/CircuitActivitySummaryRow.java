package com.dreamscale.gridtime.core.machine.executor.dashboard;

import com.dreamscale.gridtime.core.machine.clock.GeometryClock;
import com.dreamscale.gridtime.core.machine.executor.circuit.CircuitMonitor;
import com.dreamscale.gridtime.core.machine.memory.grid.cell.CellFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.*;

@Data
public class CircuitActivitySummaryRow implements Cloneable {

    private final String rowKey;
    private MonitorType monitorType;

    private Set<UUID> workers = new HashSet<>();
    private int numberWorkers;

    private int ticksProcessed;
    private int ticksFailed;

    private GeometryClock.GridTime lastGridtime;

    private LocalDateTime lastActivity;

    private ProcessStatus processStatus;

    private double executionTimeAvg;
    private double executionTimeMax;

    private double queueTimeAvg;
    private double queueTimeMax;

    private String lastFailMsg;

    private boolean isDetailRow = false;

    private LinkedList<Exception> lastFailures = new LinkedList<>();

    CircuitActivitySummaryRow(MonitorType monitorType, String rowKey, boolean isDetailRow) {
        this.monitorType = monitorType;
        this.rowKey = rowKey;
        this.isDetailRow = isDetailRow;
    }

    public void aggregateMonitor(CircuitMonitor circuitMonitor) {

        ticksProcessed += circuitMonitor.getTicksProcessed();
        ticksFailed += circuitMonitor.getTicksFailed();

        lastGridtime = maxGridtime(lastGridtime, circuitMonitor.getLastGridtime());
        lastActivity = maxDate(lastActivity, circuitMonitor.getLastStatusUpdate());

        executionTimeAvg = ((numberWorkers * executionTimeAvg) + circuitMonitor.getRecentExecutionTimeMetric().getAvg()) / (numberWorkers + 1);
        executionTimeMax = Math.max(executionTimeMax, circuitMonitor.getRecentExecutionTimeMetric().getMax());

        queueTimeAvg = ((numberWorkers * queueTimeAvg) + circuitMonitor.getRecentQueueTimeMetric().getAvg()) / (numberWorkers + 1);
        queueTimeMax = Math.max(queueTimeMax, circuitMonitor.getRecentQueueTimeMetric().getMax());

        workers.add(circuitMonitor.getWorkerId());
        numberWorkers = workers.size();

        if (circuitMonitor.getLastFailMsg() != null) {
            lastFailMsg = circuitMonitor.getLastFailMsg();
        }

        for (Exception ex : circuitMonitor.getLastFailures()) {
            pushFailure(ex);
        }

    }

    private void pushFailure(Exception ex) {
        lastFailures.push(ex);

        if (lastFailures.size() > 3) {
            lastFailures.removeLast();
        }
    }

    public void setProcessStatus(ProcessStatus processStatus) {
        this.processStatus = processStatus;
    }

    public UUID getWorkerId() {
        UUID workerId = null;

        if (workers.size() > 0) {
            workerId = workers.iterator().next();
        }
        return workerId;
    }

    public void aggregate(CircuitActivitySummaryRow activitySummary) {
        ticksProcessed += activitySummary.getTicksProcessed();
        ticksFailed += activitySummary.getTicksFailed();

        lastGridtime = maxGridtime(lastGridtime, activitySummary.getLastGridtime());
        lastActivity = maxDate(lastActivity, activitySummary.getLastActivity());

        executionTimeAvg = ((numberWorkers * executionTimeAvg) +
                (activitySummary.getNumberWorkers() * activitySummary.getExecutionTimeAvg()) / (numberWorkers + activitySummary.getNumberWorkers()));

        executionTimeMax = Math.max(executionTimeMax, activitySummary.getExecutionTimeMax());

        queueTimeAvg = ((numberWorkers * queueTimeAvg) +
                (activitySummary.getNumberWorkers() * activitySummary.getQueueTimeAvg()) / (numberWorkers + activitySummary.getNumberWorkers()));

        queueTimeMax = Math.max(queueTimeMax, activitySummary.getQueueTimeMax());

        numberWorkers += activitySummary.getNumberWorkers();

        if (activitySummary.getLastFailMsg() != null) {
            lastFailMsg = activitySummary.getLastFailMsg();
        }

        for (Exception ex : activitySummary.getLastFailures()) {
            pushFailure(ex);
        }
    }

    List<String> toRow() {
        List<String> row = new ArrayList<>();

        row.add(CellFormat.toRightSizedCell(rowKey, 18));
        row.add(CellFormat.toCell(numberWorkers, 8));
        row.add(CellFormat.toCell(ticksProcessed, 6));
        row.add(CellFormat.toCell(ticksFailed, 6));
        row.add(CellFormat.toCell(lastGridtime, 25));
        row.add(CellFormat.toCell(lastActivity, 15));
        row.add(CellFormat.toCell(executionTimeAvg, 8));
        row.add(CellFormat.toCell(executionTimeMax, 8));
        row.add(CellFormat.toCell(queueTimeAvg, 7));
        row.add(CellFormat.toCell(queueTimeMax, 7));

        if (isDetailRow) {
            row.add(CellFormat.toRightSizedCell(processStatus.name(), 9));
            row.add(CellFormat.toRightSizedCell(lastFailMsg, 40));
        }

        return row;
    }

    List<String> toHeaderRow() {
        List<String> row = new ArrayList<>();

        row.add(CellFormat.toRightSizedCell("ID", 18));
        row.add(CellFormat.toRightSizedCell("Workers", 8));
        row.add(CellFormat.toRightSizedCell("Ticks", 6));
        row.add(CellFormat.toRightSizedCell("Fails", 6));
        row.add(CellFormat.toRightSizedCell("Cursor", 25));
        row.add(CellFormat.toRightSizedCell("LastActivity", 15));
        row.add(CellFormat.toRightSizedCell("ExecAvg", 8));
        row.add(CellFormat.toRightSizedCell("ExecMax", 8));
        row.add(CellFormat.toRightSizedCell("QAvg", 7));
        row.add(CellFormat.toRightSizedCell("QMax", 7));

        if (isDetailRow) {
            row.add(CellFormat.toRightSizedCell("Status", 9));
            row.add(CellFormat.toRightSizedCell("Last Failure", 40));
        }

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

    private LocalDateTime maxDate(LocalDateTime time1, LocalDateTime time2) {
        if (time1 == null) {
            return time2;
        }
        if (time2 == null) {
            return time1;
        }

        if (time1.isAfter(time2)) {
            return time1;
        } else {
            return time2;
        }
    }

    public boolean hasFailure() {
        return lastFailMsg != null;
    }
}
