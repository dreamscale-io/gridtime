package com.dreamscale.gridtime.core.machine.executor.program;

import com.dreamscale.gridtime.core.machine.clock.Metronome;
import com.dreamscale.gridtime.core.machine.commons.DefaultCollections;
import com.dreamscale.gridtime.core.machine.executor.circuit.instructions.GenerateAggregateTile;
import com.dreamscale.gridtime.core.machine.executor.circuit.instructions.GenerateBaseTile;
import com.dreamscale.gridtime.core.machine.executor.circuit.instructions.TickInstructions;
import com.dreamscale.gridtime.core.machine.executor.program.parts.feed.FeedStrategy;
import com.dreamscale.gridtime.core.machine.executor.program.parts.locas.Locas;
import com.dreamscale.gridtime.core.machine.executor.program.parts.observer.FlowObserver;
import com.dreamscale.gridtime.core.machine.executor.program.parts.sink.FlowSink;
import com.dreamscale.gridtime.core.machine.executor.program.parts.sink.SinkStrategy;
import com.dreamscale.gridtime.core.machine.executor.program.parts.source.FlowSource;
import com.dreamscale.gridtime.core.machine.executor.program.parts.transform.FlowTransformer;
import com.dreamscale.gridtime.core.machine.executor.program.parts.transform.TransformStrategy;
import com.dreamscale.gridtime.core.machine.memory.TorchieState;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
public class SourceTileGeneratorProgram implements Program {


    private final List<Flow> pullChain = DefaultCollections.list();

    private final List<Locas> aggregatorChain = DefaultCollections.list();

    private final TorchieState torchieState;
    private final UUID torchieId;

    private final Metronome metronome;
    private final LocalDateTime runUntilPosition;

    public SourceTileGeneratorProgram(UUID torchieId, TorchieState torchieState, LocalDateTime startPosition, LocalDateTime runUntilPosition) {
        this.torchieId = torchieId;
        this.torchieState = torchieState;
        this.metronome = new Metronome(startPosition);
        this.runUntilPosition = runUntilPosition;
    }

    @Override
    public String getName() {
        return "SourceTileGenerator";
    }

    public void tick() {
        metronome.tick();

        log.debug("metronome tick: " + metronome.getActiveTick().toDisplayString());

    }

    public Metronome.TickScope getActiveTick() {
        return metronome.getActiveTick();
    }


    @Override
    public List<TickInstructions> getInstructionsAtActiveTick() {
        Metronome.TickScope tick = metronome.getActiveTick();
        List<TickInstructions> instructions = new ArrayList<>();

        instructions.add(generateBaseTickInstructions(tick));

        if (tick.hasAggregateTicks()) {
            for (Metronome.TickScope aggregateTick : tick.getAggregateTickScopes()) {
                instructions.add(generateAggregateTickInstructions(aggregateTick));
            }
        }

        return instructions;
    }

    @Override
    public int getInputQueueDepth() {
        return 0;
    }

    @Override
    public boolean isDone() {
        return metronome.getActiveTick().isAfter(runUntilPosition);
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


    private TickInstructions generateBaseTickInstructions(Metronome.TickScope tick) {
        return new GenerateBaseTile(torchieState, pullChain, tick);
    }

    private TickInstructions generateAggregateTickInstructions(Metronome.TickScope aggregateTick) {
        return new GenerateAggregateTile(torchieState, aggregatorChain, aggregateTick);
    }


    public void addAggregator( Locas locas) {
        aggregatorChain.add(locas );
    }


}
