package com.dreamscale.gridtime.core.machine.executor.circuit.instructions;

import com.dreamscale.gridtime.core.machine.capabilities.cmd.returns.Results;
import com.dreamscale.gridtime.core.machine.executor.circuit.NotifyTrigger;
import com.dreamscale.gridtime.core.machine.commons.DefaultCollections;
import com.dreamscale.gridtime.core.machine.executor.circuit.wires.TileStreamEvent;
import com.dreamscale.gridtime.core.machine.memory.tile.GridTile;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.Callable;

@Slf4j
public abstract class TickInstructions implements Callable<TickInstructions> {

    private GridTile outputTile;
    private List<Results> outputResults = DefaultCollections.list();
    private List<TileStreamEvent> outputTileStreamEvents = DefaultCollections.list();

    private Exception exceptionResult;

    private List<NotifyTrigger> notifyList = DefaultCollections.list();

    private LocalDateTime momentOfCreation;
    private LocalDateTime momentOfExecution;

    private Duration queueDuration;
    private Duration executionDuration;

    TickInstructions() {
        momentOfCreation = LocalDateTime.now();
    }

    @Override
    public TickInstructions call() {
        log.debug("Running instruction: "+getCmdDescription());
        try {
            momentOfExecution = LocalDateTime.now();
            executeInstruction();

            queueDuration = Duration.between(momentOfCreation, momentOfExecution);
            executionDuration = Duration.between(momentOfExecution, LocalDateTime.now());

        } catch (Exception ex) {
            log.error("Exception during instruction execution", ex);
            this.exceptionResult = ex;
        } finally {
            notifyAllCircuitParticipantsOnDone();
        }
        return this;
    }

    private void notifyAllCircuitParticipantsOnDone() {
        log.debug("Notify when done: "+getCmdDescription());

        for (NotifyTrigger notifyTrigger : notifyList) {
            try {
                notifyTrigger.notifyWhenDone(this, outputResults);
            } catch (Exception ex) {
                log.error("notify failed", ex);
            }
        }
    }

    public void addTriggerToNotifyList(NotifyTrigger notifyTrigger) {
        this.notifyList.add(notifyTrigger);
    }

    protected abstract void executeInstruction() throws InterruptedException;

    protected void setOutputTile(GridTile outputTile) {
        this.outputTile = outputTile;
    }

    protected void publishEvent(TileStreamEvent tileStreamEvent) {
        outputTileStreamEvents.add(tileStreamEvent);
    }

    public List<TileStreamEvent> getOutputTileStreamEvents() {
        return outputTileStreamEvents;
    }

    public void appendOutputResults(Results results) {
        outputResults.add(results);
    }

    public String getOutputResultString() {
        String out = "";

        if (outputTile != null) {
            out += outputTile.playAllTracks().toDisplayString();
        }

        for (Results results : outputResults) {
            out += results.toDisplayString();
        }

        return out;
    }

    public Results getOutputResult() {
        if (outputResults.size() > 0) {
            return outputResults.get(0);
        } else {
            return null;
        }
    }

    public List<Results> getAllOutputResults() {
        return outputResults;
    }

    public boolean isSuccessful() {
        return exceptionResult == null;
    }

    public GridTile getOutputTile() {
        return outputTile;
    }

    public abstract String getCmdDescription();

    public Duration getExecutionDuration() {
        return executionDuration;
    }

    public Duration getQueueDuration() {
        return queueDuration;
    }


}
