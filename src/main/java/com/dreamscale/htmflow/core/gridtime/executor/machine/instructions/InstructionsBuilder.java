package com.dreamscale.htmflow.core.gridtime.executor.machine.instructions;

import com.dreamscale.htmflow.core.gridtime.executor.clock.GeometryClock;
import com.dreamscale.htmflow.core.gridtime.executor.clock.Metronome;
import com.dreamscale.htmflow.core.gridtime.executor.clock.ZoomLevel;
import com.dreamscale.htmflow.core.gridtime.executor.memory.FeaturePool;
import com.dreamscale.htmflow.core.gridtime.executor.memory.grid.track.TrackSetName;

import java.time.LocalDateTime;
import java.util.UUID;

public class InstructionsBuilder {
    private final UUID torchieId;
    private final FeaturePool featurePool;
    private final Metronome metronome;

    public InstructionsBuilder(UUID torchieId, FeaturePool featurePool, Metronome metronome) {
        this.torchieId = torchieId;
        this.featurePool = featurePool;
        this.metronome = metronome;
    }

    public TileInstructions gotoTile(ZoomLevel zoomLevel, LocalDateTime clockPosition) {
        GeometryClock.Coords gotoCoords = GeometryClock.createCoords(zoomLevel, clockPosition);

        return new GotoTile(featurePool, gotoCoords);
    }

    public TileInstructions playTrack(TrackSetName trackSetName) {
        return new PlayTrack(featurePool, trackSetName);
    }

    public TileInstructions playTile() {
        return new PlayTile(featurePool);
    }

    public TileInstructions haltMetronome() {
        return new HaltMetronome(featurePool, metronome);
    }

    public TileInstructions resumeMetronome() {
        return new ResumeMetronome(featurePool, metronome);
    }

    public TileInstructions nextTile() {
        return new ManuallyForwardNextTile(featurePool);
    }


}
