package com.dreamscale.htmflow.core.gridtime.executor.machine.instructions;

import com.dreamscale.htmflow.core.gridtime.executor.machine.capabilities.cmd.returns.Results;
import com.dreamscale.htmflow.core.gridtime.executor.machine.parts.circuit.NotifyTrigger;
import com.dreamscale.htmflow.core.gridtime.executor.machine.parts.commons.DefaultCollections;
import com.dreamscale.htmflow.core.gridtime.executor.memory.tile.GridTile;
import com.dreamscale.htmflow.core.gridtime.executor.machine.parts.circuit.IdeaFlowCircuit;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.Callable;

@Slf4j
public abstract class TileInstructions implements Callable<TileInstructions> {

    private GridTile outputTile;
    private Results outputResults;

    private Exception exceptionResult;

    private List<NotifyTrigger> notifyList = DefaultCollections.list();

    private LocalDateTime momentOfCreation;
    private LocalDateTime momentOfExecution;

    private Duration queueDuration;
    private Duration executionDuration;




    TileInstructions() {
        momentOfCreation = LocalDateTime.now();
    }

    @Override
    public TileInstructions call() {
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

    protected void setOutputResults(Results outputResults) {
        this.outputResults = outputResults;
    }

    public Results getOutputResults() {
        return outputResults;
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