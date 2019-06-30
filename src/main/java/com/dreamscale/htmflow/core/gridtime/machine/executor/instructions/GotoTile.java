package com.dreamscale.htmflow.core.gridtime.machine.executor.instructions;

import com.dreamscale.htmflow.core.gridtime.machine.clock.GeometryClock;
import com.dreamscale.htmflow.core.gridtime.machine.memory.FeaturePool;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GotoTile extends TileInstructions {

    private final FeaturePool featurePool;

    private final GeometryClock.GridTime gotoPosition;


    public GotoTile(FeaturePool featurePool, GeometryClock.GridTime gotoPosition ) {
        this.featurePool = featurePool;
        this.gotoPosition = gotoPosition;
    }

    @Override
    public void executeInstruction() throws InterruptedException {
        featurePool.gotoPosition(gotoPosition);

        setOutputTile(featurePool.getActiveGridTile());
    }

    @Override
    public String getCmdDescription() {
        return "goto tile at "+gotoPosition.toDisplayString();
    }
}
