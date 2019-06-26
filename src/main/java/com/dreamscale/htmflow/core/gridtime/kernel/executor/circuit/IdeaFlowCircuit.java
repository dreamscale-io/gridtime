package com.dreamscale.htmflow.core.gridtime.kernel.executor.circuit;

import com.dreamscale.htmflow.core.gridtime.capabilities.cmd.returns.Results;
import com.dreamscale.htmflow.core.gridtime.kernel.commons.DefaultCollections;
import com.dreamscale.htmflow.core.gridtime.kernel.clock.Metronome;
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

    private LinkedList<IdeaFlowTile> recentIdeaFlowTracer;
    private IdeaFlowStateType mostRecentIdeaFlowState;

    private LinkedList<TimeBomb> activeWaits;

    private List<NotifyTrigger> notifyWhenProgramDoneTriggers = DefaultCollections.list();


    private static final int MAX_IDEA_FLOW_TILES_TO_KEEP = 4;

    public IdeaFlowCircuit(CircuitMonitor circuitMonitor, Metronome metronome) {
        this.circuitMonitor = circuitMonitor;
        this.metronome = metronome;

        this.highPriorityInstructionQueue = DefaultCollections.queueList();
        this.instructionsToExecuteQueue = DefaultCollections.queueList();
        this.recentIdeaFlowTracer = DefaultCollections.queueList();
        this.activeWaits = DefaultCollections.queueList();
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
            IdeaFlowTile ideaFlowTile = getOutputIdeaFlowTile(finishedInstruction);

            if (ideaFlowTile != null) {
                mostRecentIdeaFlowState = ideaFlowTile.getLastIdeaFlowState();
                recentIdeaFlowTracer.push(ideaFlowTile);

                if (recentIdeaFlowTracer.size() > MAX_IDEA_FLOW_TILES_TO_KEEP) {
                    recentIdeaFlowTracer.removeLast();
                }

                generateAlarms();
                triggerAlarms();
            }

            circuitMonitor.finishInstruction(finishedInstruction.getQueueDuration(), finishedInstruction.getExecutionDuration());

        }
    }

    private IdeaFlowTile getOutputIdeaFlowTile(TileInstructions finishedInstruction) {
        GridTile output = finishedInstruction.getOutputTile();

        if (output != null) {
            return output.getIdeaFlowTile();
        }
        return null;
    }

    private void generateAlarms() {
        //TODO evaluate Idea Flow Tracer, and generate TimeBomb Alarms
    }

    private void triggerAlarms() {
        //TODO evaluate all existing alarms, and generate instructions
    }

    public void scheduleInstruction(TileInstructions instructions) {
        instructionsToExecuteQueue.push(instructions);
    }

    public void scheduleHighPriorityInstruction(TileInstructions instructions) {
        highPriorityInstructionQueue.push(instructions);
    }

}
