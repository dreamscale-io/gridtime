package com.dreamscale.gridtime.core.machine.executor.circuit;

import com.dreamscale.gridtime.core.machine.capabilities.cmd.returns.Results;
import com.dreamscale.gridtime.core.machine.clock.Metronome;
import com.dreamscale.gridtime.core.machine.commons.DefaultCollections;
import com.dreamscale.gridtime.core.machine.executor.circuit.alarm.AlarmScript;
import com.dreamscale.gridtime.core.machine.executor.circuit.now.Eye;
import com.dreamscale.gridtime.core.machine.executor.circuit.now.TwilightOrangeMatrix;
import com.dreamscale.gridtime.core.machine.executor.circuit.wires.DevNullWire;
import com.dreamscale.gridtime.core.machine.executor.circuit.wires.TileStreamEvent;
import com.dreamscale.gridtime.core.machine.executor.circuit.wires.Wire;
import com.dreamscale.gridtime.core.machine.executor.program.NoOpProgram;
import com.dreamscale.gridtime.core.machine.executor.program.ParallelProgram;
import com.dreamscale.gridtime.core.machine.executor.program.Program;
import com.dreamscale.gridtime.core.machine.memory.grid.query.metrics.IdeaFlowMetrics;
import com.dreamscale.gridtime.core.machine.executor.worker.Worker;
import com.dreamscale.gridtime.core.machine.memory.tile.GridTile;
import com.dreamscale.gridtime.core.machine.executor.circuit.alarm.TimeBomb;
import com.dreamscale.gridtime.core.machine.executor.circuit.instructions.TickInstructions;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

@Slf4j
public class IdeaFlowCircuit implements Worker<TickInstructions> {

    private Program program;
    private Program nextProgram;
    private Map<UUID, ParallelProgram> parallelPrograms = DefaultCollections.map();

    private final CircuitMonitor circuitMonitor;
    private final TwilightOrangeMatrix twilightOrangeMatrix;

    private Eye eye;

    private LinkedList<TickInstructions> instructionsToExecuteQueue;
    private LinkedList<TickInstructions> highPriorityInstructionQueue;
    private TickInstructions lastInstruction;

    private LinkedList<TimeBomb> activeTimeBombMonitors;

    //circuit coordinator

    private List<NotifyTrigger> notifyWhenProgramDoneTriggers = DefaultCollections.list();

    private boolean isProgramHalted;

    private Wire outputStreamEventWire;

    public IdeaFlowCircuit(CircuitMonitor circuitMonitor) {
        this(circuitMonitor, null);
    }

    public IdeaFlowCircuit(CircuitMonitor circuitMonitor, Program program) {
        this.circuitMonitor = circuitMonitor;
        this.program = program;

        this.highPriorityInstructionQueue = DefaultCollections.queueList();
        this.instructionsToExecuteQueue = DefaultCollections.queueList();
        this.activeTimeBombMonitors = DefaultCollections.queueList();

        //eye generates this
        this.twilightOrangeMatrix = new TwilightOrangeMatrix();
        this.outputStreamEventWire = new DevNullWire();
    }

    public void configureOutputStreamEventWire(Wire outputWire) {
        this.outputStreamEventWire = outputWire;
    }

    public void fireTriggersForActiveWaits() {
        //generates instructions for actively firing triggers
    }

    public void runProgram(Program program) {
        if (program == null) {
            this.program = program;
            log.debug("Running program: "+program.getName());
        } else if (nextProgram == null) {
            log.debug("Assigning next program: "+program.getName());

            this.nextProgram = program;
        } else {
            throw new RuntimeException("Can't start program while another is in progress. There is a nextProgram already waiting.");
        }
    }

    public boolean isProgramRunning() {
        return program != null;
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

    public TickInstructions whatsNext() {

        TickInstructions nextInstruction = null;

        if (highPriorityInstructionQueue.size() > 0) {
            nextInstruction = highPriorityInstructionQueue.removeFirst();
        } else if (instructionsToExecuteQueue.size() > 0) {
            nextInstruction = instructionsToExecuteQueue.removeFirst();
        } else if (program != null && !isProgramHalted && !program.isDone()) {

            program.tick();

            instructionsToExecuteQueue.addAll(program.getInstructionsAtActiveTick());
            instructionsToExecuteQueue.addAll(getParallelProgramInstructions(program.getActiveTick()));

            if (instructionsToExecuteQueue.size() > 0) {
                nextInstruction = instructionsToExecuteQueue.removeFirst();

                circuitMonitor.updateMetronomeTickPosition(program.getActiveTick());
            }

        } else {
            fireProgramDoneTriggers();
            gotoNextProgram();
        }

        if (nextInstruction != null) {
            circuitMonitor.startInstruction();
            nextInstruction.addTriggerToNotifyList(new EvaluateOutputTrigger());
        }

        updateQueueDepth();

        lastInstruction = nextInstruction;
        return nextInstruction;
    }

    private void gotoNextProgram() {
        program = null;

        if (nextProgram != null) {
            program = nextProgram;
            nextProgram = null;
        }
    }

    private void updateQueueDepth() {
        int programQueueDepth = 0;

        if (program != null) {
            programQueueDepth = program.getInputQueueDepth();
        }


        circuitMonitor.updateQueueDepth(
                highPriorityInstructionQueue.size() +
                instructionsToExecuteQueue.size() + programQueueDepth );
    }

    private List<TickInstructions> getParallelProgramInstructions(Metronome.TickScope activeTickScope) {
        List<TickInstructions> instructionsToExecute = new ArrayList<>();

        for (ParallelProgram parallelProgram : parallelPrograms.values()) {
            instructionsToExecute.addAll(parallelProgram.getInstructionsAtTick(activeTickScope));
        }

        return instructionsToExecute;
    }

    private void fireProgramDoneTriggers() {
        fireTriggers(notifyWhenProgramDoneTriggers);
    }

    private void fireTriggers(List<NotifyTrigger> triggers) {
        for (NotifyTrigger trigger: triggers) {
            trigger.notifyWhenDone(lastInstruction, lastInstruction.getAllOutputResults());
        }
        triggers.clear();
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

    public TickInstructions getLastInstruction() {
        return lastInstruction;
    }

    public CircuitMonitor getCircuitMonitor() {
        return circuitMonitor;
    }

    private class EvaluateOutputTrigger implements NotifyTrigger {

        @Override
        public void notifyWhenDone(TickInstructions finishedInstruction, List<Results> results) {

            IdeaFlowMetrics ideaFlowMetrics = getOutputIdeaFlowMetrics(finishedInstruction);
            updateFitnessMatrixAndTriggerAlarms(ideaFlowMetrics);

            List<TileStreamEvent> tileStreamEvents = finishedInstruction.getOutputTileStreamEvents();
            outputStreamEventWire.pushAll(tileStreamEvents);

            circuitMonitor.finishInstruction(finishedInstruction.getQueueDurationMillis(), finishedInstruction.getExecutionDurationMillis());

        }

        private void updateFitnessMatrixAndTriggerAlarms(IdeaFlowMetrics ideaFlowMetrics) {
            if (ideaFlowMetrics != null) {

                twilightOrangeMatrix.push(ideaFlowMetrics);

                List<TimeBomb> timeBombMonitors = twilightOrangeMatrix.generateTimeBombMonitors();
                activeTimeBombMonitors.addAll(timeBombMonitors);

                List<AlarmScript> alarmScripts = twilightOrangeMatrix.triggerAlarms();
                for (AlarmScript script : alarmScripts) {
                    parallelPrograms.put(UUID.randomUUID(), script);
                }

            }
        }
    }

    private IdeaFlowMetrics getOutputIdeaFlowMetrics(TickInstructions finishedInstruction) {
        GridTile output = finishedInstruction.getOutputTile();

        if (output != null) {
            return output.getIdeaFlowMetrics();
        }
        return null;
    }

    public void scheduleInstruction(TickInstructions instructions) {
        instructionsToExecuteQueue.push(instructions);
    }

    public void scheduleHighPriorityInstruction(TickInstructions instructions) {
        highPriorityInstructionQueue.push(instructions);
    }

}
