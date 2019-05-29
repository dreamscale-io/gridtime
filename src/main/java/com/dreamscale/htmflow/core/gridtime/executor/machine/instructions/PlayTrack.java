package com.dreamscale.htmflow.core.gridtime.executor.machine.instructions;

import com.dreamscale.htmflow.core.gridtime.executor.memory.grid.track.TrackSetName;
import com.dreamscale.htmflow.core.gridtime.executor.machine.capabilities.cmd.returns.MusicGridResults;
import com.dreamscale.htmflow.core.gridtime.executor.memory.FeaturePool;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PlayTrack extends TileInstructions {

    private final FeaturePool featurePool;
    private final TrackSetName trackToPlay;

    public PlayTrack(FeaturePool featurePool, TrackSetName trackToPlay) {
        this.featurePool = featurePool;
        this.trackToPlay = trackToPlay;
    }

    @Override
    protected void executeInstruction() throws InterruptedException {
        MusicGridResults results = this.featurePool.getActiveGridTile().getMusicGrid().playTrackSet(trackToPlay);
        setOutputResults(results);
    }

    @Override
    public String getCmdDescription() {
        return "play track "+trackToPlay;
    }
}
