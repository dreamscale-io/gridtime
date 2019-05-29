package com.dreamscale.htmflow.core.gridtime.executor.machine;

import com.dreamscale.htmflow.core.gridtime.executor.clock.GeometryClock;
import com.dreamscale.htmflow.core.gridtime.executor.machine.instructions.InstructionsBuilder;
import com.dreamscale.htmflow.core.gridtime.executor.machine.parts.circuit.CircuitMonitor;
import com.dreamscale.htmflow.core.gridtime.executor.clock.Metronome;
import com.dreamscale.htmflow.core.gridtime.executor.machine.instructions.TileInstructions;
import com.dreamscale.htmflow.core.gridtime.executor.machine.parts.circuit.IdeaFlowCircuit;
import com.dreamscale.htmflow.core.gridtime.executor.machine.parts.fetch.FetchStrategy;
import com.dreamscale.htmflow.core.gridtime.executor.machine.parts.observer.FlowObserver;
import com.dreamscale.htmflow.core.gridtime.executor.machine.parts.sink.FlowSink;
import com.dreamscale.htmflow.core.gridtime.executor.machine.parts.sink.SinkStrategy;
import com.dreamscale.htmflow.core.gridtime.executor.machine.parts.source.FlowSource;
import com.dreamscale.htmflow.core.gridtime.executor.machine.parts.transform.FlowTransformer;
import com.dreamscale.htmflow.core.gridtime.executor.machine.parts.transform.TransformStrategy;
import com.dreamscale.htmflow.core.gridtime.executor.memory.FeaturePool;

import java.time.LocalDateTime;
import java.util.UUID;

public class Torchie {

    private final UUID torchieId;

    private final Metronome metronome;
    private final FeaturePool featurePool;

    private IdeaFlowCircuit ideaFlowCircuit;

    private CircuitMonitor circuitMonitor;

    public Torchie(UUID torchieId, FeaturePool featurePool, LocalDateTime startingPosition) {
        this.torchieId = torchieId;

        this.featurePool = featurePool;
        this.metronome = new Metronome(featurePool, startingPosition);

        this.circuitMonitor = new CircuitMonitor(torchieId);
        this.ideaFlowCircuit = new IdeaFlowCircuit(circuitMonitor, metronome);
    }

    void addFlowSourceToPullChain(FetchStrategy fetchStrategy, FlowObserver... observers) {
        metronome.addFlowToPullChain(new FlowSource(torchieId, featurePool,fetchStrategy, observers));
    }

    void addFlowTransformerToPullChain(TransformStrategy... transforms) {
        metronome.addFlowToPullChain(new FlowTransformer(torchieId, featurePool, transforms));
    }

    void addFlowSinkToPullChain(SinkStrategy... sinks) {
        metronome.addFlowToPullChain(new FlowSink(torchieId, featurePool, sinks));
    }

    public InstructionsBuilder getInstructionsBuilder() {
        return new InstructionsBuilder(torchieId, featurePool, metronome);
    }

    public void scheduleInstruction(TileInstructions instructions) {
        ideaFlowCircuit.scheduleHighPriorityInstruction(instructions);
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
