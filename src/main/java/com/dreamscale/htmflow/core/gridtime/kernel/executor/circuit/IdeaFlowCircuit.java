package com.dreamscale.htmflow.core.gridtime.kernel.executor.circuit;

import com.dreamscale.htmflow.core.gridtime.capabilities.cmd.returns.Results;
import com.dreamscale.htmflow.core.gridtime.kernel.clock.ZoomLevel;
import com.dreamscale.htmflow.core.gridtime.kernel.commons.DefaultCollections;
import com.dreamscale.htmflow.core.gridtime.kernel.clock.Metronome;
import com.dreamscale.htmflow.core.gridtime.kernel.executor.circuit.now.NowMetrics;
import com.dreamscale.htmflow.core.gridtime.kernel.memory.feature.reference.IdeaFlowStateReference;
import com.dreamscale.htmflow.core.gridtime.kernel.memory.grid.query.IdeaFlowMetrics;
import com.dreamscale.htmflow.core.gridtime.kernel.memory.tile.GridTile;
import com.dreamscale.htmflow.core.gridtime.kernel.memory.tile.IdeaFlowTile;
import com.dreamscale.htmflow.core.gridtime.kernel.executor.circuit.alarm.TimeBomb;
import com.dreamscale.htmflow.core.gridtime.kernel.executor.instructions.TileInstructions;
import com.dreamscale.htmflow.core.gridtime.kernel.memory.type.IdeaFlowStateType;

import java.util.LinkedList;
import java.util.List;

public class IdeaFlowCircuit {

    private final Metronome metronome;
    private final CircuitMonitor circuitMonitor;


    private LinkedList<TileInstructions> instructionsToExecuteQueue;
    private LinkedList<TileInstructions> highPriorityInstructionQueue;
    private TileInstructions lastInstruction;

    private LinkedList<TimeBomb> activeWaits;
    private final NowMetrics nowMetrics;

    private List<NotifyTrigger> notifyWhenProgramDoneTriggers = DefaultCollections.list();


    public IdeaFlowCircuit(CircuitMonitor circuitMonitor, Metronome metronome) {
        this.circuitMonitor = circuitMonitor;
        this.metronome = metronome;

        this.highPriorityInstructionQueue = DefaultCollections.queueList();
        this.instructionsToExecuteQueue = DefaultCollections.queueList();
        this.activeWaits = DefaultCollections.queueList();

        this.nowMetrics = new NowMetrics();
    }

    public void fireTriggersForActiveWaits() {
        //generates instructions for actively firing triggers
    }


    public TileInstructions getNextInstruction() {

        TileInstructions nextInstructions = null;

        if (highPriorityInstructionQueue.size() > 0) {
            nextInstructions = highPriorityInstructionQueue.removeFirst();
        } else if (instructionsToExecuteQueue.size() > 0) {
            nextInstructions = instructionsToExecuteQueue.removeFirst();
        } else if (metronome.canTick()) {
            instructionsToExecuteQueue.addAll(metronome.tick());
            nextInstructions = instructionsToExecuteQueue.removeFirst();

            circuitMonitor.updateTickPosition(metronome.getTickPosition());
        } else {
            fireProgramDoneTriggers();
        }

        if (nextInstructions != null) {
            circuitMonitor.startInstruction();
            nextInstructions.addTriggerToNotifyList(new EvaluateOutputTrigger());
        }

        lastInstruction = nextInstructions;
        return nextInstructions;
    }

    private void fireProgramDoneTriggers() {
        for (NotifyTrigger trigger: notifyWhenProgramDoneTriggers) {
            trigger.notifyWhenDone(lastInstruction, lastInstruction.getOutputResults());
        }
        notifyWhenProgramDoneTriggers.clear();
    }

    public void notifyWhenProgramDone(NotifyTrigger notifyTrigger) {
        this.notifyWhenProgramDoneTriggers.add(notifyTrigger);
    }

    private class EvaluateOutputTrigger implements NotifyTrigger {

        @Override
        public void notifyWhenDone(TileInstructions finishedInstruction, Results results) {
            IdeaFlowMetrics ideaFlowTile = getOutputIdeaFlowTile(finishedInstruction);

            if (ideaFlowTile != null) {

                nowMetrics.push(ideaFlowTile);

                generateAlarms();
                triggerAlarms();
            }

            circuitMonitor.finishInstruction(finishedInstruction.getQueueDuration(), finishedInstruction.getExecutionDuration());

        }
    }

    private IdeaFlowMetrics getOutputIdeaFlowTile(TileInstructions finishedInstruction) {
        GridTile output = finishedInstruction.getOutputTile();

        if (output != null) {
            return output.getIdeaFlowMetrics();
        }
        return null;
    }

    private void generateAlarms() {
        //TODO evaluate NowMetrics, and generate AlarmScripts
    }

    private void triggerAlarms() {
        //TODO evaluate all existing alarms, and run AlarmScripts as part of high priority queue
    }

    public void scheduleInstruction(TileInstructions instructions) {
        instructionsToExecuteQueue.push(instructions);
    }

    public void scheduleHighPriorityInstruction(TileInstructions instructions) {
        highPriorityInstructionQueue.push(instructions);
    }

}
