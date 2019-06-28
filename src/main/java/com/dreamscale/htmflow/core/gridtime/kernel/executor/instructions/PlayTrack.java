package com.dreamscale.htmflow.core.gridtime.kernel.executor.instructions;

import com.dreamscale.htmflow.core.gridtime.kernel.memory.grid.query.key.TrackSetKey;
import com.dreamscale.htmflow.core.gridtime.capabilities.cmd.returns.MusicGridResults;
import com.dreamscale.htmflow.core.gridtime.kernel.memory.FeaturePool;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PlayTrack extends TileInstructions {

    private final FeaturePool featurePool;
    private final TrackSetKey trackToPlay;

    public PlayTrack(FeaturePool featurePool, TrackSetKey trackToPlay) {
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
