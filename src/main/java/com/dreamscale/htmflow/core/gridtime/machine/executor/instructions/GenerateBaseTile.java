package com.dreamscale.htmflow.core.gridtime.machine.executor.instructions;

import com.dreamscale.htmflow.core.gridtime.machine.clock.Metronome;
import com.dreamscale.htmflow.core.gridtime.machine.executor.circuit.Flow;
import com.dreamscale.htmflow.core.gridtime.machine.memory.TorchieState;
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
        for (Flow flow : pullChain) {
            flow.tick(tick);
        }

        setOutputTile(torchieState.getActiveTile());
    }

    @Override
    public String getCmdDescription() {
        return "generate tile for "+tick.toDisplayString();
    }
}
