package com.dreamscale.gridtime.core.machine.executor.circuit.instructions;

import com.dreamscale.gridtime.api.grid.Results;
import com.dreamscale.gridtime.core.machine.executor.circuit.NotifyFailureTrigger;
import com.dreamscale.gridtime.core.machine.executor.circuit.NotifyDoneTrigger;
import com.dreamscale.gridtime.core.machine.commons.DefaultCollections;
import com.dreamscale.gridtime.core.machine.executor.circuit.wires.Notifier;
import com.dreamscale.gridtime.core.machine.executor.circuit.wires.TileStreamEvent;
import com.dreamscale.gridtime.core.machine.memory.tile.GridTile;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.Callable;

@Slf4j
public abstract class TickInstructions implements Callable<TickInstructions>, Notifier {

    private GridTile outputTile;
    private List<Results> outputResults = DefaultCollections.list();
    private List<TileStreamEvent> outputTileStreamEvents = DefaultCollections.list();

    private Exception exceptionResult;

    private List<NotifyDoneTrigger> notifyDoneTriggers = DefaultCollections.list();
    private List<NotifyFailureTrigger> notifyOnFailureTriggers = DefaultCollections.list();

    private long momentOfCreation;
    private long momentOfExecution;

    private long queueDurationMillis;
    private long executionDurationMillis;

    TickInstructions() {
        momentOfCreation = System.currentTimeMillis();
    }

    @Override
    public TickInstructions call() {
        log.debug("Running instruction: "+getCmdDescription());
        try {
            momentOfExecution = System.currentTimeMillis();
            queueDurationMillis = momentOfExecution - momentOfCreation;

            executeInstruction();

            executionDurationMillis = System.currentTimeMillis() - momentOfExecution;

        } catch (Exception ex) {
            log.error("Exception during instruction execution", ex);
            this.exceptionResult = ex;
            executionDurationMillis = System.currentTimeMillis() - momentOfExecution;

            notifyAllCircuitParticipantsOnError();
        } finally {
            if (exceptionResult == null) {
                notifyAllCircuitParticipantsOnDone();
            }
        }
        return this;
    }

    private void notifyAllCircuitParticipantsOnError() {
        log.debug("Notify on error: "+getCmdDescription());

        for (NotifyFailureTrigger notifyTrigger : notifyOnFailureTriggers) {
            try {
                notifyTrigger.notifyOnAbortOrFailure(this, exceptionResult);
            } catch (Exception ex) {
                log.error("notify failed", ex);
            }
        }
    }

    private void notifyAllCircuitParticipantsOnDone() {
        log.debug("Notify when done: "+getCmdDescription());

        for (NotifyDoneTrigger notifyTrigger : notifyDoneTriggers) {
            try {
                notifyTrigger.notifyWhenDone(this, outputResults);
            } catch (Exception ex) {
                log.error("notify failed", ex);
            }
        }
    }

    public void notifyOnDone(NotifyDoneTrigger notifyTrigger) {
        this.notifyDoneTriggers.add(notifyTrigger);
    }

    public void notifyOnFail(NotifyFailureTrigger notifyTrigger) {
        this.notifyOnFailureTriggers.add(notifyTrigger);
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

    public boolean isFailure() {
        return exceptionResult != null;
    }


    public Exception getExceptionResult() {
        return exceptionResult;
    }

    public GridTile getOutputTile() {
        return outputTile;
    }

    public abstract String getCmdDescription();

    public long getExecutionDurationMillis() {
        return executionDurationMillis;
    }

    public long getQueueDurationMillis() {
        return queueDurationMillis;
    }


}
