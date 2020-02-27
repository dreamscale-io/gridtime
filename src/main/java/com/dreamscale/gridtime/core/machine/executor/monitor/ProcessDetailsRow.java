package com.dreamscale.gridtime.core.machine.executor.monitor;

import com.dreamscale.gridtime.core.machine.clock.GeometryClock;
import com.dreamscale.gridtime.core.machine.clock.ZoomLevel;
import com.dreamscale.gridtime.core.machine.executor.circuit.CircuitMonitor;
import com.dreamscale.gridtime.core.machine.memory.grid.cell.CellFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
public class ProcessDetailsRow {


    private UUID workerId;

    private LocalDateTime jobStartTime;
    private LocalDateTime lastStatusUpdate;

    private int metronomeTicksProcessed;
    private int ticksProcessed;

    private ZoomLevel zoomLevel;

    private GeometryClock.GridTime gridtime;

    private double executionTimeAvg;
    private double executionTimeMax;

    private double queueTimeAvg;
    private double queueTimeMax;

    private long totalExec;
    private long totalQueue;


    private int queueDepth;


    ProcessDetailsRow(CircuitMonitor circuitMonitor) {
        workerId = circuitMonitor.getWorkerId();

        jobStartTime = circuitMonitor.getJobStartTime();
        lastStatusUpdate = circuitMonitor.getLastStatusUpdate();

        ticksProcessed = circuitMonitor.getTicksProcessed();
        metronomeTicksProcessed = circuitMonitor.getMetronomeTicksProcessed();

        if (circuitMonitor.getActiveTickScopePosition() != null) {
            zoomLevel = circuitMonitor.getActiveTickScopePosition().getZoomLevel();
            gridtime = circuitMonitor.getActiveTickScopePosition().getFrom();
        }

        executionTimeAvg = circuitMonitor.getRecentExecutionTimeMetric().getAvg();
        executionTimeMax = circuitMonitor.getRecentExecutionTimeMetric().getMax();

        queueTimeAvg = circuitMonitor.getRecentQueueTimeMetric().getAvg();
        queueTimeAvg = circuitMonitor.getRecentQueueTimeMetric().getMax();

        totalExec = circuitMonitor.getTotalExecutionTime();
        totalQueue = circuitMonitor.getTotalQueueTime();

        queueDepth = circuitMonitor.getQueueDepth();
    }

    public String getProcessId() {
        return CellFormat.toCellValue(workerId);
    }

    List<String> toHeaderRow() {
        List<String> row = new ArrayList<>();

        row.add(CellFormat.toRightSizedCell("", 10));
        row.add(CellFormat.toRightSizedCell("ID", 8));
        row.add(CellFormat.toRightSizedCell("StartedOn", 10));
        row.add(CellFormat.toRightSizedCell("LastUpdate", 10));
        row.add(CellFormat.toRightSizedCell("Ticks", 5));
        row.add(CellFormat.toRightSizedCell("Metronome", 5));
        row.add(CellFormat.toRightSizedCell("Cursor", 7));
        row.add(CellFormat.toRightSizedCell("TotalExec", 10));
        row.add(CellFormat.toRightSizedCell("TotalQ", 10));
        row.add(CellFormat.toRightSizedCell("ExecMax", 7));
        row.add(CellFormat.toRightSizedCell("ExecAvg", 7));
        row.add(CellFormat.toRightSizedCell("QAvg", 5));
        row.add(CellFormat.toRightSizedCell("QMax", 5));
        row.add(CellFormat.toRightSizedCell("QDepth", 5));

        return row;
    }

    List<String> toRow(String rowKey) {
        List<String> row = new ArrayList<>();

        row.add(CellFormat.toRightSizedCell(rowKey, 10));
        row.add(CellFormat.toCell(workerId, 7));
        row.add(CellFormat.toCell(jobStartTime, 5));
        row.add(CellFormat.toCell(lastStatusUpdate, 10));
        row.add(CellFormat.toCell(ticksProcessed, 7));
        row.add(CellFormat.toCell(metronomeTicksProcessed, 7));
        row.add(CellFormat.toCell(gridtime, 5));

        row.add(CellFormat.toDurationCell(totalExec, 10));
        row.add(CellFormat.toDurationCell(totalQueue, 10));
        row.add(CellFormat.toCell(executionTimeAvg, 5));
        row.add(CellFormat.toCell(executionTimeMax, 5));
        row.add(CellFormat.toCell(queueTimeAvg, 5));
        row.add(CellFormat.toCell(queueTimeMax, 5));
        row.add(CellFormat.toCell(queueDepth, 5));

        return row;
    }

    public ProcessDetailsRow clone() {
        try {
            return (ProcessDetailsRow) super.clone();
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
