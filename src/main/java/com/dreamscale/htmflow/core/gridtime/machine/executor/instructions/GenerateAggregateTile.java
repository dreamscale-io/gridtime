package com.dreamscale.htmflow.core.gridtime.machine.executor.instructions;

import com.dreamscale.htmflow.core.gridtime.machine.clock.Metronome;
import com.dreamscale.htmflow.core.gridtime.machine.clock.ZoomLevel;
import com.dreamscale.htmflow.core.gridtime.machine.executor.program.parts.locas.Locas;
import com.dreamscale.htmflow.core.gridtime.machine.memory.TorchieState;
import com.dreamscale.htmflow.core.gridtime.machine.memory.grid.AggregateGrid;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class GenerateAggregateTile extends TileInstructions {

    private final TorchieState torchieState;
    private final Metronome.Tick tick;
    private final List<Locas> aggregatorChain;

    public GenerateAggregateTile(TorchieState torchieState, List<Locas> aggregatorChain, Metronome.Tick tick) {
        this.torchieState = torchieState;
        this.aggregatorChain = aggregatorChain;
        this.tick = tick;
    }

    @Override
    protected void executeInstruction() throws InterruptedException {

        for (Locas aggregator : aggregatorChain) {
            AggregateGrid output = aggregator.runProgram(tick);
            appendOutputResults(output.playAllTracks());
        }
    }

    @Override
    public String getCmdDescription() {
        return "generate aggregate tile for "+tick.getZoomLevel() + " anchored at "+ tick.toDisplayString() ;
    }
}
