package com.dreamscale.gridtime.core.machine;

import com.dreamscale.gridtime.core.machine.capabilities.cmd.returns.MusicGridResults;
import com.dreamscale.gridtime.core.machine.clock.Metronome;
import com.dreamscale.gridtime.core.machine.executor.circuit.NotifyTrigger;
import com.dreamscale.gridtime.core.machine.executor.circuit.instructions.InstructionsBuilder;
import com.dreamscale.gridtime.core.machine.executor.circuit.CircuitMonitor;
import com.dreamscale.gridtime.core.machine.executor.circuit.instructions.TileInstructions;
import com.dreamscale.gridtime.core.machine.executor.circuit.TwilightCircuit;
import com.dreamscale.gridtime.core.machine.executor.circuit.wires.Wire;
import com.dreamscale.gridtime.core.machine.executor.program.Program;
import com.dreamscale.gridtime.core.machine.executor.program.parts.feed.FeedStrategyFactory;
import com.dreamscale.gridtime.core.machine.executor.worker.Worker;
import com.dreamscale.gridtime.core.machine.memory.TorchieState;
import com.dreamscale.gridtime.core.machine.memory.feed.InputFeed;
import com.dreamscale.gridtime.core.machine.memory.feed.Flowable;

import java.util.UUID;

public class Torchie implements Worker<TileInstructions> {

    private final UUID torchieId;

    private final TorchieState torchieState;

    private TwilightCircuit twilightCircuit;

    private CircuitMonitor circuitMonitor;

    public Torchie(UUID torchieId, TorchieState torchieState, Program program) {
        this.torchieId = torchieId;

        this.torchieState = torchieState;

        this.circuitMonitor = new CircuitMonitor(torchieId);
        this.twilightCircuit = new TwilightCircuit(circuitMonitor, program);
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
        twilightCircuit.scheduleHighPriorityInstruction(instructions);
    }

    public void notifyWhenProgramDone(NotifyTrigger notifyTrigger) {
        twilightCircuit.notifyWhenProgramDone(notifyTrigger);
    }

    public TileInstructions pullNext() {
        return twilightCircuit.pullNext();
    }

    public boolean isWorkerReady() {
        return twilightCircuit.isWorkerReady();
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
        twilightCircuit.haltProgram();
    }

    public void resumeProgram() {
        twilightCircuit.resumeProgram();
    }

    public Metronome.TickScope getActiveTick() {
        return twilightCircuit.getActiveTick();
    }

    public MusicGridResults playAllTracks() {
        return torchieState.getActiveTile().playAllTracks();
    }


    public void configureOutputStreamEventWire(Wire outputWire) {
        twilightCircuit.configureOutputStreamEventWire(outputWire);
    }

}
