package com.dreamscale.gridtime.core.machine.executor.circuit.instructions;

import com.dreamscale.gridtime.core.domain.work.WorkToDoType;
import com.dreamscale.gridtime.core.machine.clock.Metronome;
import com.dreamscale.gridtime.core.machine.executor.circuit.wires.TileStreamEvent;
import com.dreamscale.gridtime.core.machine.executor.program.parts.locas.Locas;
import com.dreamscale.gridtime.core.machine.memory.TorchieState;
import com.dreamscale.gridtime.core.machine.memory.grid.IMusicGrid;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class GenerateAggregateTile extends TileInstructions {

    private final TorchieState torchieState;
    private final Metronome.TickScope tickScope;
    private final List<Locas> aggregatorChain;

    public GenerateAggregateTile(TorchieState torchieState, List<Locas> aggregatorChain, Metronome.TickScope tickScope) {
        this.torchieState = torchieState;
        this.aggregatorChain = aggregatorChain;
        this.tickScope = tickScope;
    }

    @Override
    protected void executeInstruction() throws InterruptedException {

        for (Locas aggregator : aggregatorChain) {
            IMusicGrid output = aggregator.runProgram(tickScope);
            appendOutputResults(output.playAllTracks());
        }

        publishEvent(new TileStreamEvent(torchieState.getTeamId(), torchieState.getTorchieId(), tickScope.getFrom(), WorkToDoType.AggregateToTeam));
    }

    @Override
    public String getCmdDescription() {
        return "generate aggregate tile for "+ tickScope.getZoomLevel() + " anchored at "+ tickScope.toDisplayString() ;
    }
}
