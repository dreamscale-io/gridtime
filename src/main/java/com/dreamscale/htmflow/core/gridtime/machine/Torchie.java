package com.dreamscale.htmflow.core.gridtime.machine;

import com.dreamscale.htmflow.core.gridtime.machine.executor.circuit.NotifyTrigger;
import com.dreamscale.htmflow.core.gridtime.machine.executor.instructions.InstructionsBuilder;
import com.dreamscale.htmflow.core.gridtime.machine.executor.program.MetronomeProgram;
import com.dreamscale.htmflow.core.gridtime.machine.executor.circuit.CircuitMonitor;
import com.dreamscale.htmflow.core.gridtime.machine.clock.Metronome;
import com.dreamscale.htmflow.core.gridtime.machine.executor.instructions.TileInstructions;
import com.dreamscale.htmflow.core.gridtime.machine.executor.circuit.IdeaFlowCircuit;
import com.dreamscale.htmflow.core.gridtime.machine.memory.FeaturePool;

import java.util.UUID;

public class Torchie {

    private final UUID torchieId;

    private final Metronome metronome;
    private final FeaturePool featurePool;

    private IdeaFlowCircuit ideaFlowCircuit;

    private CircuitMonitor circuitMonitor;

    public Torchie(UUID torchieId, FeaturePool featurePool, MetronomeProgram metronomeJob) {
        this.torchieId = torchieId;

        this.featurePool = featurePool;
        this.metronome = new Metronome(metronomeJob);

        this.circuitMonitor = new CircuitMonitor(torchieId);
        this.ideaFlowCircuit = new IdeaFlowCircuit(circuitMonitor, metronome);
    }

    public InstructionsBuilder getInstructionsBuilder() {
        return new InstructionsBuilder(torchieId, featurePool, metronome);
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
}
