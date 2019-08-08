package com.dreamscale.htmflow.core.gridtime.machine.executor.circuit;

import com.dreamscale.htmflow.core.gridtime.capabilities.cmd.returns.Results;
import com.dreamscale.htmflow.core.gridtime.machine.clock.Metronome;
import com.dreamscale.htmflow.core.gridtime.machine.commons.DefaultCollections;
import com.dreamscale.htmflow.core.gridtime.machine.executor.circuit.alarm.AlarmScript;
import com.dreamscale.htmflow.core.gridtime.machine.executor.circuit.now.FitnessMatrix;
import com.dreamscale.htmflow.core.gridtime.machine.executor.circuit.wires.DevNullWire;
import com.dreamscale.htmflow.core.gridtime.machine.executor.circuit.wires.TileStreamEvent;
import com.dreamscale.htmflow.core.gridtime.machine.executor.circuit.wires.Wire;
import com.dreamscale.htmflow.core.gridtime.machine.executor.program.ParallelProgram;
import com.dreamscale.htmflow.core.gridtime.machine.executor.program.Program;
import com.dreamscale.htmflow.core.gridtime.machine.executor.program.parts.analytics.query.IdeaFlowMetrics;
import com.dreamscale.htmflow.core.gridtime.machine.memory.tile.GridTile;
import com.dreamscale.htmflow.core.gridtime.machine.executor.circuit.alarm.TimeBomb;
import com.dreamscale.htmflow.core.gridtime.machine.executor.circuit.instructions.TileInstructions;

import java.util.*;

public class IdeaFlowCircuit {

    private final CircuitMonitor circuitMonitor;

    private final Program program;
    private Map<UUID, ParallelProgram> parallelPrograms = DefaultCollections.map();

    private LinkedList<TileInstructions> instructionsToExecuteQueue;
    private LinkedList<TileInstructions> highPriorityInstructionQueue;
    private TileInstructions lastInstruction;

    private LinkedList<TimeBomb> activeTimeBombMonitors;
    private final FitnessMatrix fitnessMatrix;

    //circuit coordinator

    private List<NotifyTrigger> notifyWhenProgramDoneTriggers = DefaultCollections.list();
    private boolean isProgramHalted;

    private Wire inputStreamEventWire;
    private Wire outputStreamEventWire;


    public IdeaFlowCircuit(CircuitMonitor circuitMonitor, Program program) {
        this.circuitMonitor = circuitMonitor;
        this.program = program;

        this.highPriorityInstructionQueue = DefaultCollections.queueList();
        this.instructionsToExecuteQueue = DefaultCollections.queueList();
        this.activeTimeBombMonitors = DefaultCollections.queueList();

        this.fitnessMatrix = new FitnessMatrix();
        this.outputStreamEventWire = new DevNullWire();
        this.inputStreamEventWire = new DevNullWire();
    }

    public void configureOutputStreamEventWire(Wire outputWire) {
        this.outputStreamEventWire = outputWire;
    }

    public void configureInputStreamEventWire(Wire inputWire) {
        inputStreamEventWire = inputWire;
    }

    public void fireTriggersForActiveWaits() {
        //generates instructions for actively firing triggers
    }

    public void haltProgram() {
        this.isProgramHalted = true;
    }

    public void resumeProgram() {
        isProgramHalted = false;
    }

    public void runParallelProgram(UUID programId, ParallelProgram parallelProgram) {
        parallelPrograms.put(programId, parallelProgram);
    }

    public void removeParallelProgram(UUID programId) {
        parallelPrograms.remove(programId);
    }

    public TileInstructions getNextInstruction() {

        TileInstructions nextInstruction = null;

        if (highPriorityInstructionQueue.size() > 0) {
            nextInstruction = highPriorityInstructionQueue.removeFirst();
        } else if (instructionsToExecuteQueue.size() > 0) {
            nextInstruction = instructionsToExecuteQueue.removeFirst();
        } else if (!isProgramHalted && !program.isDone()) {

            program.tick(inputStreamEventWire);

            instructionsToExecuteQueue.addAll(program.getInstructionsAtActiveTick());
            instructionsToExecuteQueue.addAll(getParallelProgramInstructions(program.getActiveTick()));

            if (instructionsToExecuteQueue.size() > 0) {
                nextInstruction = instructionsToExecuteQueue.removeFirst();

                circuitMonitor.updateTickPosition(program.getActiveTick());
            }

        } else {
            fireProgramDoneTriggers();
        }

        if (nextInstruction != null) {
            circuitMonitor.startInstruction();
            nextInstruction.addTriggerToNotifyList(new EvaluateOutputTrigger());
        }


        circuitMonitor.updateQueueDepth(
                highPriorityInstructionQueue.size() +
                instructionsToExecuteQueue.size() +
                inputStreamEventWire.getQueueDepth());

        lastInstruction = nextInstruction;
        return nextInstruction;
    }

    private List<TileInstructions> getParallelProgramInstructions(Metronome.Tick activeTick) {
        List<TileInstructions> instructionsToExecute = new ArrayList<>();

        for (ParallelProgram parallelProgram : parallelPrograms.values()) {
            instructionsToExecute.addAll(parallelProgram.getInstructionsAtTick(activeTick));
        }

        return instructionsToExecute;
    }

    private void fireProgramDoneTriggers() {
        for (NotifyTrigger trigger: notifyWhenProgramDoneTriggers) {
            trigger.notifyWhenDone(lastInstruction, lastInstruction.getAllOutputResults());
        }
        notifyWhenProgramDoneTriggers.clear();
    }

    public void notifyWhenProgramDone(NotifyTrigger notifyTrigger) {
        this.notifyWhenProgramDoneTriggers.add(notifyTrigger);
    }

    public Metronome.Tick getActiveTick() {
        return program.getActiveTick();
    }



    private class EvaluateOutputTrigger implements NotifyTrigger {

        @Override
        public void notifyWhenDone(TileInstructions finishedInstruction, List<Results> results) {

            IdeaFlowMetrics ideaFlowMetrics = getOutputIdeaFlowMetrics(finishedInstruction);
            updateFitnessMatrixAndTriggerAlarms(ideaFlowMetrics);

            List<TileStreamEvent> tileStreamEvents = finishedInstruction.getOutputTileStreamEvents();
            outputStreamEventWire.publishAll(tileStreamEvents);

            circuitMonitor.finishInstruction(finishedInstruction.getQueueDuration(), finishedInstruction.getExecutionDuration());

        }

        private void updateFitnessMatrixAndTriggerAlarms(IdeaFlowMetrics ideaFlowMetrics) {
            if (ideaFlowMetrics != null) {

                fitnessMatrix.push(ideaFlowMetrics);

                List<TimeBomb> timeBombMonitors = fitnessMatrix.generateTimeBombMonitors();
                activeTimeBombMonitors.addAll(timeBombMonitors);

                List<AlarmScript> alarmScripts = fitnessMatrix.triggerAlarms();
                for (AlarmScript script : alarmScripts) {
                    parallelPrograms.put(UUID.randomUUID(), script);
                }

            }
        }
    }

    private IdeaFlowMetrics getOutputIdeaFlowMetrics(TileInstructions finishedInstruction) {
        GridTile output = finishedInstruction.getOutputTile();

        if (output != null) {
            return output.getIdeaFlowMetrics();
        }
        return null;
    }

    public void scheduleInstruction(TileInstructions instructions) {
        instructionsToExecuteQueue.push(instructions);
    }

    public void scheduleHighPriorityInstruction(TileInstructions instructions) {
        highPriorityInstructionQueue.push(instructions);
    }

}
