package com.dreamscale.gridtime.core.machine.executor.circuit.instructions;

import com.dreamscale.gridtime.core.domain.work.WorkToDoType;
import com.dreamscale.gridtime.core.machine.clock.Metronome;
import com.dreamscale.gridtime.core.machine.executor.program.Flow;
import com.dreamscale.gridtime.core.machine.executor.circuit.wires.TileStreamEvent;
import com.dreamscale.gridtime.core.machine.memory.TorchieState;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class GenerateBaseTile extends TileInstructions {

    private final List<Flow> pullChain;
    private final TorchieState torchieState;

    private final Metronome.TickScope tickScope;


    public GenerateBaseTile(TorchieState torchieState, List<Flow> pullChain, Metronome.TickScope tickScope) {
        this.torchieState = torchieState;
        this.pullChain = pullChain;
        this.tickScope = tickScope;
    }

    @Override
    public void executeInstruction() throws InterruptedException {

        torchieState.gotoPosition(tickScope.getFrom());

        for (Flow flow : pullChain) {
            flow.tick(tickScope);
        }

        setOutputTile(torchieState.getActiveTile());

        publishEvent(new TileStreamEvent(torchieState.getTeamId(), torchieState.getTorchieId(), tickScope.getFrom(), WorkToDoType.AggregateToTeam));
    }

    @Override
    public String getCmdDescription() {
        return "generate tile for "+ tickScope.toDisplayString();
    }
}
