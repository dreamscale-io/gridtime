package com.dreamscale.gridtime.core.machine.executor.circuit;

import com.dreamscale.gridtime.api.grid.Results;
import com.dreamscale.gridtime.core.machine.clock.Metronome;
import com.dreamscale.gridtime.core.machine.commons.DefaultCollections;
import com.dreamscale.gridtime.core.machine.executor.circuit.alarm.AlarmScript;
import com.dreamscale.gridtime.core.machine.executor.circuit.now.Eye;
import com.dreamscale.gridtime.core.machine.executor.circuit.now.TwilightOrangeMatrix;
import com.dreamscale.gridtime.core.machine.executor.circuit.wires.DevNullWire;
import com.dreamscale.gridtime.core.machine.executor.circuit.wires.TileStreamEvent;
import com.dreamscale.gridtime.core.machine.executor.circuit.wires.Wire;
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
public class IdeaFlowCircuit implements Worker {

    private Program program;
    private Program nextProgram;
    private final Map<UUID, ParallelProgram> parallelPrograms = DefaultCollections.map();

    private final CircuitMonitor circuitMonitor;
    private final TwilightOrangeMatrix twilightOrangeMatrix;

    private Eye eye;

    private final LinkedList<TickInstructions> instructionsToExecuteQueue;
    private final LinkedList<TickInstructions> highPriorityInstructionQueue;
    private TickInstructions lastInstruction;

    private final LinkedList<TimeBomb> activeTimeBombMonitors;

    //circuit coordinator

    private final List<NotifyDoneTrigger> notifyWhenProgramDoneTriggers = DefaultCollections.list();
    private final List<NotifyFailureTrigger> notifyWhenProgramFailsTriggers = DefaultCollections.list();

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
        if (this.program == null) {
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

    public void clearProgram() {
        program = null;
        nextProgram = null;

        highPriorityInstructionQueue.clear();
        instructionsToExecuteQueue.clear();

        notifyAllProgramAborted();

        notifyWhenProgramDoneTriggers.clear();
        notifyWhenProgramFailsTriggers.clear();
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
            nextInstruction.addNotifyOnDoneTrigger(new EvaluateOutputTrigger());
            nextInstruction.addNotifyOnErrorTrigger(new TerminateProgramTrigger());
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
        if (program != null) {
            notifyAllProgramIsDone();

            notifyWhenProgramDoneTriggers.clear();
            notifyWhenProgramFailsTriggers.clear();
        }
    }

    private void notifyAllProgramIsDone() {
        log.debug("Notify all: Program is done. Program " + program.getName() + " terminated.");

        List<Results> outputResults = null;
        if (lastInstruction != null) {
            outputResults = lastInstruction.getAllOutputResults();
        }

        for (NotifyDoneTrigger trigger: notifyWhenProgramDoneTriggers) {
            trigger.notifyWhenDone(lastInstruction, outputResults);
        }
    }

    private void notifyAllProgramAborted() {
        for (NotifyFailureTrigger trigger: notifyWhenProgramFailsTriggers) {

            Exception ex = null;

            if (lastInstruction != null) {
                ex = lastInstruction.getExceptionResult();
            }

            trigger.notifyOnAbortOrFailure(lastInstruction, ex);
        }
    }

    public void notifyWhenProgramDone(NotifyDoneTrigger notifyTrigger) {
        this.notifyWhenProgramDoneTriggers.add(notifyTrigger);
    }

    public void notifyWhenProgramFails(NotifyFailureTrigger notifyTrigger) {
        this.notifyWhenProgramFailsTriggers.add(notifyTrigger);
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

    public UUID getWorkerId() {
        return circuitMonitor.getWorkerId();
    }


    private class TerminateProgramTrigger implements NotifyFailureTrigger {

        @Override
        public void notifyOnAbortOrFailure(TickInstructions finishedInstruction, Exception ex) {

            circuitMonitor.failInstruction(finishedInstruction.getQueueDurationMillis(), finishedInstruction.getExecutionDurationMillis(), ex.getMessage());

            log.error("Terminating program because of failed instruction:" + ex);

            gotoNextProgram();
            notifyAllProgramAborted();
        }
    }

    private class EvaluateOutputTrigger implements NotifyDoneTrigger {

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
