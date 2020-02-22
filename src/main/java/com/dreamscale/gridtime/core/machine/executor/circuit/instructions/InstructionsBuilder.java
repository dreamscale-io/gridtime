package com.dreamscale.gridtime.core.machine.executor.circuit.instructions;

import com.dreamscale.gridtime.core.machine.clock.GeometryClock;
import com.dreamscale.gridtime.core.machine.clock.ZoomLevel;
import com.dreamscale.gridtime.core.machine.memory.TorchieState;
import com.dreamscale.gridtime.core.machine.memory.grid.query.key.TrackSetKey;

import java.time.LocalDateTime;
import java.util.UUID;

public class InstructionsBuilder {
    private final UUID torchieId;
    private final TorchieState torchieState;

    public InstructionsBuilder(UUID torchieId, TorchieState torchieState) {
        this.torchieId = torchieId;
        this.torchieState = torchieState;
    }

    public TickInstructions gotoTile(ZoomLevel zoomLevel, LocalDateTime clockPosition) {
        GeometryClock.GridTime gotoGridTime = GeometryClock.createGridTime(zoomLevel, clockPosition);

        return new GotoTile(torchieState, gotoGridTime);
    }

    public TickInstructions playTrack(TrackSetKey trackSetName) {
        return new PlayTrack(torchieState, trackSetName);
    }

    public TickInstructions playTile() {
        return new PlayTile(torchieState);
    }

    public TickInstructions nextTile() {
        return new ManuallyForwardNextTile(torchieState);
    }


}
