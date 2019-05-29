package com.dreamscale.htmflow.core.gridtime.executor.machine.instructions;

import com.dreamscale.htmflow.core.gridtime.executor.clock.GeometryClock;
import com.dreamscale.htmflow.core.gridtime.executor.clock.Metronome;
import com.dreamscale.htmflow.core.gridtime.executor.memory.FeaturePool;
import com.dreamscale.htmflow.core.gridtime.executor.memory.tile.GridTile;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

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
            GeometryClock.Coords nextCoords = activeTile.getGridCoordinates().panRight();
            log.debug("Forwarding tile to "+nextCoords.getFormattedGridTime());

            featurePool.nextGridTile(nextCoords);
        } else {
            log.error("Unable to forward to next tile, active tile is null");
        }

    }

    @Override
    public String getCmdDescription() {
        return "manually ticking forward next tile";
    }
}
