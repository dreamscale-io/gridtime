package com.dreamscale.htmflow.core.gridtime.executor.machine.instructions;

import com.dreamscale.htmflow.core.gridtime.executor.machine.capabilities.cmd.returns.MusicGridResults;
import com.dreamscale.htmflow.core.gridtime.executor.memory.FeaturePool;
import com.dreamscale.htmflow.core.gridtime.executor.memory.grid.track.TrackSetName;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PlayTile extends TileInstructions {

    private final FeaturePool featurePool;

    public PlayTile(FeaturePool featurePool) {
        this.featurePool = featurePool;
    }

    @Override
    protected void executeInstruction() throws InterruptedException {
        MusicGridResults results = this.featurePool.getActiveGridTile().getMusicGrid().playAllTracks();
        setOutputResults(results);
    }

    @Override
    public String getCmdDescription() {
        return "play tile "+featurePool.getActiveGridTime();
    }
}
