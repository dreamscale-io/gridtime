package com.dreamscale.htmflow.core.gridtime.machine.executor.instructions;

import com.dreamscale.htmflow.core.gridtime.capabilities.cmd.returns.MusicGridResults;
import com.dreamscale.htmflow.core.gridtime.machine.memory.FeaturePool;
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
