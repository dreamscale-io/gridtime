package com.dreamscale.htmflow.core.gridtime.machine.executor.instructions;

import com.dreamscale.htmflow.core.gridtime.machine.clock.GeometryClock;
import com.dreamscale.htmflow.core.gridtime.machine.clock.Metronome;
import com.dreamscale.htmflow.core.gridtime.machine.clock.ZoomLevel;
import com.dreamscale.htmflow.core.gridtime.machine.memory.FeaturePool;
import com.dreamscale.htmflow.core.gridtime.machine.memory.grid.query.key.TrackSetKey;

import java.time.LocalDateTime;
import java.util.UUID;

public class InstructionsBuilder {
    private final UUID torchieId;
    private final FeaturePool featurePool;

    public InstructionsBuilder(UUID torchieId, FeaturePool featurePool) {
        this.torchieId = torchieId;
        this.featurePool = featurePool;
    }

    public TileInstructions gotoTile(ZoomLevel zoomLevel, LocalDateTime clockPosition) {
        GeometryClock.GridTime gotoGridTime = GeometryClock.createGridTime(zoomLevel, clockPosition);

        return new GotoTile(featurePool, gotoGridTime);
    }

    public TileInstructions playTrack(TrackSetKey trackSetName) {
        return new PlayTrack(featurePool, trackSetName);
    }

    public TileInstructions playTile() {
        return new PlayTile(featurePool);
    }

    public TileInstructions nextTile() {
        return new ManuallyForwardNextTile(featurePool);
    }


}
