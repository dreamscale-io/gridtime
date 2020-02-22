package com.dreamscale.gridtime.core.machine;

import com.dreamscale.gridtime.core.machine.clock.Metronome;
import com.dreamscale.gridtime.core.machine.executor.circuit.NotifyTrigger;
import com.dreamscale.gridtime.core.machine.executor.circuit.instructions.InstructionsBuilder;
import com.dreamscale.gridtime.core.machine.executor.circuit.CircuitMonitor;
import com.dreamscale.gridtime.core.machine.executor.circuit.instructions.TickInstructions;
import com.dreamscale.gridtime.core.machine.executor.circuit.IdeaFlowCircuit;
import com.dreamscale.gridtime.core.machine.executor.circuit.wires.Wire;
import com.dreamscale.gridtime.core.machine.executor.program.Program;
import com.dreamscale.gridtime.core.machine.executor.program.parts.feed.FeedStrategyFactory;
import com.dreamscale.gridtime.core.machine.executor.worker.Worker;
import com.dreamscale.gridtime.core.machine.memory.TorchieState;
import com.dreamscale.gridtime.core.machine.memory.box.TeamBoxConfiguration;
import com.dreamscale.gridtime.core.machine.memory.feed.InputFeed;
import com.dreamscale.gridtime.core.machine.memory.feed.Flowable;

import java.util.UUID;

public class Torchie implements Worker<TickInstructions> {

    private final UUID torchieId;

    private final TorchieState torchieState;

    private IdeaFlowCircuit ideaFlowCircuit;

    private CircuitMonitor circuitMonitor;

    public Torchie(UUID torchieId, TorchieState torchieState, Program program) {
        this.torchieId = torchieId;

        this.torchieState = torchieState;

        this.circuitMonitor = new CircuitMonitor(torchieId);
        this.ideaFlowCircuit = new IdeaFlowCircuit(circuitMonitor, program);
    }

    public <T extends Flowable> InputFeed<T> getInputFeed(FeedStrategyFactory.FeedType type) {
        return torchieState.getInputFeed(type);
    }

    public void changeBoxConfiguration(TeamBoxConfiguration teamBoxConfiguration) {
        torchieState.changeBoxConfiguration(teamBoxConfiguration);
    }


    public String whatTimeIsIt() {
        return torchieState.getActiveTime();
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

    public void notifyWhenProgramDone(NotifyTrigger notifyTrigger) {
        ideaFlowCircuit.notifyWhenProgramDone(notifyTrigger);
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

}
