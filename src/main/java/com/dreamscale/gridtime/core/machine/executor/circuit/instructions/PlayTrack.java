package com.dreamscale.gridtime.core.machine.executor.circuit.instructions;

import com.dreamscale.gridtime.core.machine.memory.grid.query.key.TrackSetKey;
import com.dreamscale.gridtime.api.grid.GridTableResults;
import com.dreamscale.gridtime.core.machine.memory.TorchieState;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PlayTrack extends TickInstructions {

    private final TorchieState torchieState;
    private final TrackSetKey trackToPlay;

    public PlayTrack(TorchieState torchieState, TrackSetKey trackToPlay) {
        this.torchieState = torchieState;
        this.trackToPlay = trackToPlay;
    }

    @Override
    protected void executeInstruction() throws InterruptedException {
        GridTableResults results = this.torchieState.getActiveTile().playTrack(trackToPlay);
        appendOutputResults(results);
    }

    @Override
    public String getCmdDescription() {
        return "play track "+trackToPlay;
    }
}
