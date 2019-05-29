package com.dreamscale.htmflow.core.gridtime.executor.machine.instructions;

import com.dreamscale.htmflow.core.gridtime.executor.clock.GeometryClock;
import com.dreamscale.htmflow.core.gridtime.executor.machine.parts.commons.Flow;
import com.dreamscale.htmflow.core.gridtime.executor.memory.FeaturePool;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class GotoTile extends TileInstructions {

    private final FeaturePool featurePool;

    private final GeometryClock.Coords gotoPosition;


    public GotoTile(FeaturePool featurePool, GeometryClock.Coords gotoPosition ) {
        this.featurePool = featurePool;
        this.gotoPosition = gotoPosition;
    }

    @Override
    public void executeInstruction() throws InterruptedException {
        featurePool.gotoGridTile(gotoPosition);

        setOutputTile(featurePool.getActiveGridTile());
    }

    @Override
    public String getCmdDescription() {
        return "goto tile at "+gotoPosition.getFormattedGridTime();
    }
}
