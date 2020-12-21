package com.dreamscale.gridtime.core.machine.executor.circuit.instructions;

import com.dreamscale.gridtime.api.grid.GridTableResults;
import com.dreamscale.gridtime.core.machine.memory.TorchieState;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PlayTile extends TickInstructions {

    private final TorchieState torchieState;

    public PlayTile(TorchieState torchieState) {
        this.torchieState = torchieState;
    }

    @Override
    protected void executeInstruction() throws InterruptedException {
        GridTableResults results = this.torchieState.getActiveTile().playAllTracks();
        appendOutputResults(results);
    }

    @Override
    public String getCmdDescription() {
        return "play tile "+ torchieState.getActiveGridTime();
    }
}
