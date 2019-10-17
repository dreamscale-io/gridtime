package com.dreamscale.gridtime.core.machine.executor.circuit;

import com.dreamscale.gridtime.core.machine.capabilities.cmd.returns.Results;
import com.dreamscale.gridtime.core.machine.clock.Metronome;
import com.dreamscale.gridtime.core.machine.commons.DefaultCollections;
import com.dreamscale.gridtime.core.machine.executor.circuit.alarm.AlarmScript;
import com.dreamscale.gridtime.core.machine.executor.circuit.now.Eye;
import com.dreamscale.gridtime.core.machine.executor.circuit.now.TwilightFitnessMatrix;
import com.dreamscale.gridtime.core.machine.executor.circuit.wires.DevNullWire;
import com.dreamscale.gridtime.core.machine.executor.circuit.wires.TileStreamEvent;
import com.dreamscale.gridtime.core.machine.executor.circuit.wires.Wire;
import com.dreamscale.gridtime.core.machine.executor.program.ParallelProgram;
import com.dreamscale.gridtime.core.machine.executor.program.Program;
import com.dreamscale.gridtime.core.machine.memory.grid.query.metrics.IdeaFlowMetrics;
import com.dreamscale.gridtime.core.machine.executor.worker.Worker;
import com.dreamscale.gridtime.core.machine.memory.tile.GridTile;
import com.dreamscale.gridtime.core.machine.executor.circuit.alarm.TimeBomb;
import com.dreamscale.gridtime.core.machine.executor.circuit.instructions.TileInstructions;

import java.util.*;

public class TwilightCircuit implements Worker<TileInstructions> {

    private Program program;
    private Map<UUID, ParallelProgram> parallelPrograms = DefaultCollections.map();

    private final CircuitMonitor circuitMonitor;
    private final TwilightFitnessMatrix twilightFitnessMatrix;

    private Eye eye;

    private LinkedList<TileInstructions> instructionsToExecuteQueue;
    private LinkedList<TileInstructions> highPriorityInstructionQueue;
    private TileInstructions lastInstruction;

    private LinkedList<TimeBomb> activeTimeBombMonitors;

    //circuit coordinator

    private List<NotifyTrigger> notifyWhenProgramDoneTriggers = DefaultCollections.list();
    private boolean isProgramHalted;

    private Wire outputStreamEventWire;

    public TwilightCircuit(CircuitMonitor circuitMonitor, Program program) {
        this.circuitMonitor = circuitMonitor;
        this.program = program;

        this.highPriorityInstructionQueue = DefaultCollections.queueList();
        this.instructionsToExecuteQueue = DefaultCollections.queueList();
        this.activeTimeBombMonitors = DefaultCollections.queueList();

        this.twilightFitnessMatrix = new TwilightFitnessMatrix();
        this.outputStreamEventWire = new DevNullWire();
    }

    public void configureOutputStreamEventWire(Wire outputWire) {
        this.outputStreamEventWire = outputWire;
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

    public TileInstructions whatsNext() {

        TileInstructions nextInstruction = null;

        if (highPriorityInstructionQueue.size() > 0) {
            nextInstruction = highPriorityInstructionQueue.removeFirst();
        } else if (instructionsToExecuteQueue.size() > 0) {
            nextInstruction = instructionsToExecuteQueue.removeFirst();
        } else if (!isProgramHalted && !program.isDone()) {

            program.tick();

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
                instructionsToExecuteQueue.size() + program.getInputQueueDepth() );

        lastInstruction = nextInstruction;
        return nextInstruction;
    }

    private List<TileInstructions> getParallelProgramInstructions(Metronome.TickScope activeTickScope) {
        List<TileInstructions> instructionsToExecute = new ArrayList<>();

        for (ParallelProgram parallelProgram : parallelPrograms.values()) {
            instructionsToExecute.addAll(parallelProgram.getInstructionsAtTick(activeTickScope));
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

    public Metronome.TickScope getActiveTick() {
        return program.getActiveTick();
    }

    public boolean isWorkerReady() {
        return circuitMonitor.isReady();
    }


    private class EvaluateOutputTrigger implements NotifyTrigger {

        @Override
        public void notifyWhenDone(TileInstructions finishedInstruction, List<Results> results) {

            IdeaFlowMetrics ideaFlowMetrics = getOutputIdeaFlowMetrics(finishedInstruction);
            updateFitnessMatrixAndTriggerAlarms(ideaFlowMetrics);

            List<TileStreamEvent> tileStreamEvents = finishedInstruction.getOutputTileStreamEvents();
            outputStreamEventWire.pushAll(tileStreamEvents);

            circuitMonitor.finishInstruction(finishedInstruction.getQueueDuration(), finishedInstruction.getExecutionDuration());

        }

        private void updateFitnessMatrixAndTriggerAlarms(IdeaFlowMetrics ideaFlowMetrics) {
            if (ideaFlowMetrics != null) {

                twilightFitnessMatrix.push(ideaFlowMetrics);

                List<TimeBomb> timeBombMonitors = twilightFitnessMatrix.generateTimeBombMonitors();
                activeTimeBombMonitors.addAll(timeBombMonitors);

                List<AlarmScript> alarmScripts = twilightFitnessMatrix.triggerAlarms();
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