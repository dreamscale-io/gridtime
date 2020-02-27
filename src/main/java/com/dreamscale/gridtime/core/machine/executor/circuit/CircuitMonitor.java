package com.dreamscale.gridtime.core.machine.executor.circuit;

import com.dreamscale.gridtime.core.machine.clock.Metronome;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Data
public class CircuitMonitor {

    private ProcessType processType;

    private UUID workerId;
    private LocalDateTime jobStartTime;
    private LocalDateTime lastStatusUpdate;

    private WindowMetric recentExecutionTimeMetric = new WindowMetric(10);
    private WindowMetric recentQueueTimeMetric = new WindowMetric(10);

    private long lastExecutionDuration;
    private long lastQueueDuration;
    private int ticksProcessed;
    private int metronomeTicksProcessed;

    private long totalExecutionTime;
    private long totalQueueTime;

    private Metronome.TickScope activeTickScopePosition;
    private int queueDepth;

    private State state;


    public CircuitMonitor(ProcessType processType, UUID workerId) {
        this.processType = processType;
        this.workerId = workerId;
        this.jobStartTime = LocalDateTime.now();
        this.state = State.Ready;

        updateStatusTimestamp();
    }

    public void startInstruction() {
        log.debug("Setting circuit state to busy!");
        state = State.Busy;
        ticksProcessed++;

        updateStatusTimestamp();
    }

    public void finishInstruction(long queueDurationMillis, long executionDurationMillis) {
        log.debug("Setting circuit state to ready!");
        state = State.Ready;
        lastQueueDuration = queueDurationMillis;
        lastExecutionDuration = executionDurationMillis;

        totalExecutionTime += lastExecutionDuration;
        totalQueueTime += lastQueueDuration;

        recentExecutionTimeMetric.addSample(lastExecutionDuration);
        recentQueueTimeMetric.addSample(lastQueueDuration);

        updateStatusTimestamp();
    }

    public void updateMetronomeTickPosition(Metronome.TickScope activeTickScopePosition) {
        this.activeTickScopePosition = activeTickScopePosition;

        metronomeTicksProcessed++;
    }

    private void updateStatusTimestamp() {
        lastStatusUpdate = LocalDateTime.now();
    }

    public boolean isReady() {
        return state == State.Ready;
    }

    public boolean isBusy() {
        return state == State.Busy;
    }

    public void updateQueueDepth(int depth) {
        this.queueDepth = depth;
    }

    public int getQueueDepth() {
        return queueDepth;
    }



    public enum State {
        Ready,
        Busy
    }
}
