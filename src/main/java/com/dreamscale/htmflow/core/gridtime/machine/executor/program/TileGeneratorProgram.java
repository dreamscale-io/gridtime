package com.dreamscale.htmflow.core.gridtime.machine.executor.program;

import com.dreamscale.htmflow.core.gridtime.machine.clock.Metronome;
import com.dreamscale.htmflow.core.gridtime.machine.commons.DefaultCollections;
import com.dreamscale.htmflow.core.gridtime.machine.executor.circuit.Flow;
import com.dreamscale.htmflow.core.gridtime.machine.executor.instructions.GenerateAggregateUpTile;
import com.dreamscale.htmflow.core.gridtime.machine.executor.instructions.GenerateBaseTile;
import com.dreamscale.htmflow.core.gridtime.machine.executor.instructions.TileInstructions;
import com.dreamscale.htmflow.core.gridtime.machine.executor.program.parts.fetch.FetchStrategy;
import com.dreamscale.htmflow.core.gridtime.machine.executor.program.parts.observer.FlowObserver;
import com.dreamscale.htmflow.core.gridtime.machine.executor.program.parts.sink.FlowSink;
import com.dreamscale.htmflow.core.gridtime.machine.executor.program.parts.sink.SinkStrategy;
import com.dreamscale.htmflow.core.gridtime.machine.executor.program.parts.source.FlowSource;
import com.dreamscale.htmflow.core.gridtime.machine.executor.program.parts.transform.FlowTransformer;
import com.dreamscale.htmflow.core.gridtime.machine.executor.program.parts.transform.TransformStrategy;
import com.dreamscale.htmflow.core.gridtime.machine.memory.FeaturePool;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TileGeneratorProgram implements Program {


    private final List<Flow> pullChain = DefaultCollections.list();

    private final FeaturePool featurePool;
    private final UUID torchieId;

    private final Metronome metronome;

    public TileGeneratorProgram(UUID torchieId, FeaturePool featurePool, LocalDateTime startPosition) {
        this.torchieId = torchieId;
        this.featurePool = featurePool;
        this.metronome = new Metronome(startPosition);

        featurePool.gotoPosition(metronome.getActivePosition());
    }

    public void tick() {
        metronome.tick();
    }

    //this one publishes up wires..?
    //different types of instructions, can change the program being operated.

    //so if my tick, says I've got aggregate up a tick, and that's the type of program I run, is the AggregateUp program,
    //then when the first program is run, I should do my little rollup thing.

    public Metronome.Tick getActiveTick() {
        return metronome.getActiveTick();
    }

    public LocalDateTime suggestDefaultResumeLocation() {
        return null;
    }

    @Override
    public List<TileInstructions> getInstructionsAtTick(Metronome.Tick tick) {

        List<TileInstructions> instructions = new ArrayList<>();

        instructions.add(generateBaseTickInstructions(tick));

        if (tick.hasAggregateTicks()) {
            for (Metronome.Tick aggregateTick : tick.getAggregateTicks()) {
                instructions.add(generateAggregateTickInstructions(aggregateTick));
            }
        }

        return instructions;
    }

    @Override
    public List<TileInstructions> getInstructionsAtActiveTick() {
        Metronome.Tick tick = metronome.getActiveTick();
        return getInstructionsAtTick(tick);
    }

    @Override
    public boolean isDone() {
        return false;
    }

    public void addFlowSourceToPullChain(FetchStrategy fetchStrategy, FlowObserver... observers) {
        pullChain.add(new FlowSource(torchieId, featurePool,fetchStrategy, observers));
    }

    public void addFlowTransformerToPullChain(TransformStrategy... transforms) {
        pullChain.add(new FlowTransformer(torchieId, featurePool, transforms));
    }

    public void addFlowSinkToPullChain(SinkStrategy... sinks) {
        pullChain.add(new FlowSink(torchieId, featurePool, sinks));
    }


    private TileInstructions generateBaseTickInstructions(Metronome.Tick tick) {
        return new GenerateBaseTile(featurePool, pullChain, tick);
    }

    //aggregate chain, would have aggregate source, aggregate sink?
    //then wire into the aggregate up instruction

    private TileInstructions generateAggregateTickInstructions(Metronome.Tick aggregateTick) {
        //other chain

        return new GenerateAggregateUpTile(featurePool, aggregateTick);
    }


}
