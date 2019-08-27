package com.dreamscale.gridtime.core.machine.executor.circuit.instructions;

import com.dreamscale.gridtime.core.machine.clock.Metronome;
import com.dreamscale.gridtime.core.machine.executor.program.parts.locas.Locas;
import com.dreamscale.gridtime.core.machine.memory.grid.IMusicGrid;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class GenerateTeamTile extends TileInstructions {

    private final Metronome.Tick tick;
    private final List<Locas> aggregatorChain;

    public GenerateTeamTile(List<Locas> aggregatorChain, Metronome.Tick tick) {
        this.aggregatorChain = aggregatorChain;
        this.tick = tick;
    }

    @Override
    protected void executeInstruction() throws InterruptedException {

        for (Locas aggregator : aggregatorChain) {
            IMusicGrid output = aggregator.runProgram(tick);
            appendOutputResults(output.playAllTracks());
        }

    }

    @Override
    public String getCmdDescription() {
        return "generate team aggregate tile for "+tick.getZoomLevel() + " anchored at "+ tick.toDisplayString() ;
    }
}
