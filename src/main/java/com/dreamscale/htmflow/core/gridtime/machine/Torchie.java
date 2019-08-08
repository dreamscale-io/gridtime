package com.dreamscale.htmflow.core.gridtime.machine;

import com.dreamscale.htmflow.core.gridtime.capabilities.cmd.returns.MusicGridResults;
import com.dreamscale.htmflow.core.gridtime.machine.clock.Metronome;
import com.dreamscale.htmflow.core.gridtime.machine.executor.circuit.NotifyTrigger;
import com.dreamscale.htmflow.core.gridtime.machine.executor.circuit.instructions.InstructionsBuilder;
import com.dreamscale.htmflow.core.gridtime.machine.executor.circuit.CircuitMonitor;
import com.dreamscale.htmflow.core.gridtime.machine.executor.circuit.instructions.TileInstructions;
import com.dreamscale.htmflow.core.gridtime.machine.executor.circuit.IdeaFlowCircuit;
import com.dreamscale.htmflow.core.gridtime.machine.executor.circuit.wires.AggregatingWire;
import com.dreamscale.htmflow.core.gridtime.machine.executor.circuit.wires.Wire;
import com.dreamscale.htmflow.core.gridtime.machine.executor.program.Program;
import com.dreamscale.htmflow.core.gridtime.machine.executor.program.parts.feed.FeedStrategyFactory;
import com.dreamscale.htmflow.core.gridtime.machine.memory.TorchieState;
import com.dreamscale.htmflow.core.gridtime.machine.memory.feed.InputFeed;
import com.dreamscale.htmflow.core.gridtime.machine.memory.feed.Flowable;

import java.util.UUID;

public class Torchie {

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

    public void scheduleInstruction(TileInstructions instructions) {
        ideaFlowCircuit.scheduleHighPriorityInstruction(instructions);
    }

    public void notifyWhenProgramDone(NotifyTrigger notifyTrigger) {
        ideaFlowCircuit.notifyWhenProgramDone(notifyTrigger);
    }

    public TileInstructions whatsNext() {
        return ideaFlowCircuit.getNextInstruction();
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

    public Metronome.Tick getActiveTick() {
        return ideaFlowCircuit.getActiveTick();
    }

    public MusicGridResults playAllTracks() {
        return torchieState.getActiveTile().playAllTracks();
    }


    public void configureOutputStreamEventWire(Wire outputWire) {
        ideaFlowCircuit.configureOutputStreamEventWire(outputWire);
    }

    public void configureInputStreamEventWire(Wire inputWire) {
        ideaFlowCircuit.configureInputStreamEventWire(inputWire);
    }
}
