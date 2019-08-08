package com.dreamscale.htmflow.core.gridtime.machine.executor.program;

import com.dreamscale.htmflow.core.gridtime.machine.clock.Metronome;
import com.dreamscale.htmflow.core.gridtime.machine.commons.DefaultCollections;
import com.dreamscale.htmflow.core.gridtime.machine.executor.circuit.instructions.GenerateAggregateTile;
import com.dreamscale.htmflow.core.gridtime.machine.executor.circuit.instructions.GenerateBaseTile;
import com.dreamscale.htmflow.core.gridtime.machine.executor.circuit.instructions.TileInstructions;
import com.dreamscale.htmflow.core.gridtime.machine.executor.circuit.wires.Wire;
import com.dreamscale.htmflow.core.gridtime.machine.executor.program.parts.feed.FeedStrategy;
import com.dreamscale.htmflow.core.gridtime.machine.executor.program.parts.locas.Locas;
import com.dreamscale.htmflow.core.gridtime.machine.executor.program.parts.locas.ZoomableTimeLocas;
import com.dreamscale.htmflow.core.gridtime.machine.executor.program.parts.observer.FlowObserver;
import com.dreamscale.htmflow.core.gridtime.machine.executor.program.parts.sink.FlowSink;
import com.dreamscale.htmflow.core.gridtime.machine.executor.program.parts.sink.SinkStrategy;
import com.dreamscale.htmflow.core.gridtime.machine.executor.program.parts.source.FlowSource;
import com.dreamscale.htmflow.core.gridtime.machine.executor.program.parts.transform.FlowTransformer;
import com.dreamscale.htmflow.core.gridtime.machine.executor.program.parts.transform.TransformStrategy;
import com.dreamscale.htmflow.core.gridtime.machine.memory.TorchieState;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
public class TileGeneratorProgram implements Program {


    private final List<Flow> pullChain = DefaultCollections.list();

    private final List<Locas> aggregatorChain = DefaultCollections.list();

    private final TorchieState torchieState;
    private final UUID torchieId;

    private final Metronome metronome;
    private  boolean isInitialized;
    private Wire outputStreamEventWire;

    public TileGeneratorProgram(UUID torchieId, TorchieState torchieState, LocalDateTime startPosition) {
        this.torchieId = torchieId;
        this.torchieState = torchieState;
        this.metronome = new Metronome(startPosition);

        this.isInitialized = false;
    }

    public void tick(Wire inputStreamEventWire) {
        metronome.tick();

        log.debug("metronome tick: " + metronome.getActiveTick().toDisplayString());

    }

    public Metronome.Tick getActiveTick() {
        return metronome.getActiveTick();
    }


    @Override
    public List<TileInstructions> getInstructionsAtActiveTick() {
        Metronome.Tick tick = metronome.getActiveTick();
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
    public boolean isDone() {
        return false;
    }

    public void addFlowSourceToPullChain(FeedStrategy feedStrategy, FlowObserver... observers) {
        pullChain.add(new FlowSource(torchieId, torchieState, feedStrategy, observers));
    }

    public void addFlowTransformerToPullChain(TransformStrategy... transforms) {
        pullChain.add(new FlowTransformer(torchieId, torchieState, transforms));
    }

    public void addFlowSinkToPullChain(SinkStrategy... sinks) {
        pullChain.add(new FlowSink(torchieId, torchieState, sinks));
    }


    private TileInstructions generateBaseTickInstructions(Metronome.Tick tick) {
        return new GenerateBaseTile(torchieState, pullChain, tick);
    }

    private TileInstructions generateAggregateTickInstructions(Metronome.Tick aggregateTick) {

        return new GenerateAggregateTile(torchieState, aggregatorChain, aggregateTick);
    }


    public void addAggregator( ZoomableTimeLocas locas) {
        aggregatorChain.add(locas );
    }


}
