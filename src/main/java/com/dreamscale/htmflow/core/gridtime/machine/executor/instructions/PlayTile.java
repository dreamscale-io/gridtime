package com.dreamscale.htmflow.core.gridtime.machine.executor.instructions;

import com.dreamscale.htmflow.core.gridtime.capabilities.cmd.returns.MusicGridResults;
import com.dreamscale.htmflow.core.gridtime.machine.memory.TorchieState;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PlayTile extends TileInstructions {

    private final TorchieState torchieState;

    public PlayTile(TorchieState torchieState) {
        this.torchieState = torchieState;
    }

    @Override
    protected void executeInstruction() throws InterruptedException {
        MusicGridResults results = this.torchieState.getActiveTile().playAllTracks();
        setOutputResults(results);
    }

    @Override
    public String getCmdDescription() {
        return "play tile "+ torchieState.getActiveTime();
    }
}
