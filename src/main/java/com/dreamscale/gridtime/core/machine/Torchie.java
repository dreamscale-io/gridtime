package com.dreamscale.gridtime.core.machine;

import com.dreamscale.gridtime.core.machine.capabilities.cmd.TorchieCmd;
import com.dreamscale.gridtime.core.machine.clock.GeometryClock;
import com.dreamscale.gridtime.core.machine.clock.Metronome;
import com.dreamscale.gridtime.core.machine.executor.circuit.*;
import com.dreamscale.gridtime.core.machine.executor.circuit.instructions.InstructionsBuilder;
import com.dreamscale.gridtime.core.machine.executor.circuit.instructions.TickInstructions;
import com.dreamscale.gridtime.core.machine.executor.circuit.now.Eye;
import com.dreamscale.gridtime.core.machine.executor.circuit.wires.Notifier;
import com.dreamscale.gridtime.core.machine.executor.circuit.wires.Wire;
import com.dreamscale.gridtime.core.machine.executor.program.Program;
import com.dreamscale.gridtime.core.machine.executor.program.ProgramFactory;
import com.dreamscale.gridtime.core.machine.executor.program.parts.feed.FeedStrategyFactory;
import com.dreamscale.gridtime.core.machine.executor.worker.Worker;
import com.dreamscale.gridtime.core.machine.memory.TorchieState;
import com.dreamscale.gridtime.core.machine.memory.box.BoxResolver;
import com.dreamscale.gridtime.core.machine.memory.feed.InputFeed;
import com.dreamscale.gridtime.core.machine.memory.feed.Flowable;

import java.time.LocalDateTime;
import java.util.UUID;

public class Torchie implements Worker, Notifier {

    private final UUID torchieId;

    private final TorchieState torchieState;

    private final IdeaFlowCircuit ideaFlowCircuit;

    private final CircuitMonitor circuitMonitor;

    private final Eye eye;


    public Torchie(UUID torchieId, TorchieState torchieState, Program defaultProgram) {
        this.torchieId = torchieId;

        this.torchieState = torchieState;

        this.circuitMonitor = new CircuitMonitor(ProcessType.Torchie, torchieId);
        this.ideaFlowCircuit = new IdeaFlowCircuit(circuitMonitor, defaultProgram);

        this.eye = new Eye();

        torchieState.monitorWith(eye);
    }

    public <T extends Flowable> InputFeed<T> getInputFeed(FeedStrategyFactory.FeedType type) {
        return torchieState.getInputFeed(type);
    }

    public void changeBoxConfiguration(BoxResolver boxResolver) {
        torchieState.changeBoxConfiguration(boxResolver);
    }


    public void sees() {
        //lets take a sampling of diagnostic instructions, and throw them on high priority queue
        //on the circuit.  And tell you a summary of what Torchie is thinkin about right now.

        //run parallel program, with a metronome sync
    }

    public InstructionsBuilder getInstructionsBuilder() {
        return new InstructionsBuilder(torchieId, torchieState);
    }

    public void scheduleInstruction(TickInstructions instructions) {
        ideaFlowCircuit.scheduleHighPriorityInstruction(instructions);
    }

    public void notifyOnDone(NotifyDoneTrigger notifyTrigger) {
        ideaFlowCircuit.notifyOnDone(notifyTrigger);
    }

    public void notifyOnFail(NotifyFailureTrigger notifyTrigger) {
        ideaFlowCircuit.notifyOnFail(notifyTrigger);
    }


    public TickInstructions whatsNext() {
        return ideaFlowCircuit.whatsNext();
    }

    public boolean isWorkerReady() {
        return ideaFlowCircuit.isWorkerReady();
    }

    public CircuitMonitor getCircuitMonitor() {
        return circuitMonitor;
    }

    public UUID getTorchieId() {
        return torchieId;
    }

    public void serializeForSleep() {

    }

    public void haltProgram() {
        ideaFlowCircuit.haltProgram();
    }

    public void resumeProgram() {
        ideaFlowCircuit.resumeProgram();
    }

    public Metronome.TickScope getActiveTick() {
        return ideaFlowCircuit.getActiveTick();
    }

    public String getLastOutput() {
        TickInstructions lastInstruction = ideaFlowCircuit.getLastInstruction();
        if (lastInstruction != null) {
            return lastInstruction.getOutputResultString();
        }

        return "";
    }


    public void configureOutputStreamEventWire(Wire outputWire) {
        ideaFlowCircuit.configureOutputStreamEventWire(outputWire);
    }

    public GeometryClock.GridTime getActiveGridTime() {
        return torchieState.getActiveGridTime();
    }

    public void waitForCircuitReady() {
        circuitMonitor.waitForReady();
    }

    public void abortAndClearProgram() {
        ideaFlowCircuit.abortAndClearProgram();
    }

    public void watchForGridtime(GeometryClock.GridTime timeToWatch, NotifySeeTrigger notifySeeTrigger) {
        eye.watchForGridtime(timeToWatch, notifySeeTrigger);
    }

    public TickInstructions getLastInstruction() {
        return ideaFlowCircuit.getLastInstruction();
    }
}
