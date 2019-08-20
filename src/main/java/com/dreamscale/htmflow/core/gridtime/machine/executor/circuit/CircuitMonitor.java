package com.dreamscale.htmflow.core.gridtime.machine.executor.circuit;

import com.dreamscale.htmflow.core.gridtime.machine.clock.Metronome;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Slf4j
public class CircuitMonitor {

    private UUID torchieId;
    private LocalDateTime jobStartTime;
    private LocalDateTime lastStatusUpdate;

    //TODO eventually will turn these to candle stick monitors
    private Duration lastExecutionDuration;
    private Duration lastQueueDuration;
    private int instructionsProcessed;
    private int ticksProcessed;

    private Metronome.Tick activeTickPosition;
    private int queueDepth;

    private State state;



    public CircuitMonitor(UUID torchieId) {
        this.torchieId = torchieId;
        this.jobStartTime = LocalDateTime.now();
        this.state = State.Ready;

        updateStatusTimestamp();
    }

    public void startInstruction() {
        log.debug("Setting circuit state to busy!");
        state = State.Busy;
        instructionsProcessed++;

        updateStatusTimestamp();
    }

    public void finishInstruction(Duration queueDuration, Duration executionDuration) {
        log.debug("Setting circuit state to ready!");
        state = State.Ready;
        lastQueueDuration = queueDuration;
        lastExecutionDuration = executionDuration;

        updateStatusTimestamp();
    }

    public void updateTickPosition(Metronome.Tick activeTickPosition) {
        this.activeTickPosition = activeTickPosition;

        ticksProcessed++;
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
