package com.dreamscale.htmflow.core.gridtime.machine.executor.instructions;

import com.dreamscale.htmflow.core.gridtime.machine.clock.GeometryClock;
import com.dreamscale.htmflow.core.gridtime.machine.clock.ZoomLevel;
import com.dreamscale.htmflow.core.gridtime.machine.memory.TorchieState;
import com.dreamscale.htmflow.core.gridtime.machine.memory.grid.query.key.TrackSetKey;

import java.time.LocalDateTime;
import java.util.UUID;

public class InstructionsBuilder {
    private final UUID torchieId;
    private final TorchieState torchieState;

    public InstructionsBuilder(UUID torchieId, TorchieState torchieState) {
        this.torchieId = torchieId;
        this.torchieState = torchieState;
    }

    public TileInstructions gotoTile(ZoomLevel zoomLevel, LocalDateTime clockPosition) {
        GeometryClock.GridTime gotoGridTime = GeometryClock.createGridTime(zoomLevel, clockPosition);

        return new GotoTile(torchieState, gotoGridTime);
    }

    public TileInstructions playTrack(TrackSetKey trackSetName) {
        return new PlayTrack(torchieState, trackSetName);
    }

    public TileInstructions playTile() {
        return new PlayTile(torchieState);
    }

    public TileInstructions nextTile() {
        return new ManuallyForwardNextTile(torchieState);
    }


}
