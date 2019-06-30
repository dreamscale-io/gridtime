package com.dreamscale.htmflow.core.gridtime.machine.executor.instructions;

import com.dreamscale.htmflow.core.gridtime.machine.clock.GeometryClock;
import com.dreamscale.htmflow.core.gridtime.machine.memory.FeaturePool;
import com.dreamscale.htmflow.core.gridtime.machine.memory.tile.GridTile;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ManuallyForwardNextTile extends TileInstructions {

    private final FeaturePool featurePool;

    public ManuallyForwardNextTile(FeaturePool featurePool) {
        this.featurePool = featurePool;
    }

    @Override
    protected void executeInstruction() throws InterruptedException {

        GridTile activeTile = featurePool.getActiveGridTile();
        if (activeTile != null) {
            GeometryClock.GridTime nextGridTime = activeTile.getGridTime().panRight();
            log.debug("Forwarding tile to "+ nextGridTime.toDisplayString());

            featurePool.nextGridTile(nextGridTime);
        } else {
            log.error("Unable to forward to next tile, active tile is null");
        }

    }

    @Override
    public String getCmdDescription() {
        return "manually ticking forward next tile";
    }
}
