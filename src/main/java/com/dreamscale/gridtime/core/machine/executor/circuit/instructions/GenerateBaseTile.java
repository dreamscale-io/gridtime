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

    private final Metronome.Tick tick;


    public GenerateBaseTile(TorchieState torchieState, List<Flow> pullChain, Metronome.Tick tick) {
        this.torchieState = torchieState;
        this.pullChain = pullChain;
        this.tick = tick;
    }

    @Override
    public void executeInstruction() throws InterruptedException {

        torchieState.gotoPosition(tick.getFrom());

        for (Flow flow : pullChain) {
            flow.tick(tick);
        }

        setOutputTile(torchieState.getActiveTile());

        publishEvent(new TileStreamEvent(torchieState.getTeamId(), torchieState.getTorchieId(), tick.getFrom(), WorkToDoType.AggregateToTeam));
    }

    @Override
    public String getCmdDescription() {
        return "generate tile for "+tick.toDisplayString();
    }
}
