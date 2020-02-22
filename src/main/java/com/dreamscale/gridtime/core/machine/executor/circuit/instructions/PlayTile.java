package com.dreamscale.gridtime.core.machine.executor.circuit.instructions;

import com.dreamscale.gridtime.core.machine.capabilities.cmd.returns.MusicGridResults;
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
        MusicGridResults results = this.torchieState.getActiveTile().playAllTracks();
        appendOutputResults(results);
    }

    @Override
    public String getCmdDescription() {
        return "play tile "+ torchieState.getActiveTime();
    }
}
