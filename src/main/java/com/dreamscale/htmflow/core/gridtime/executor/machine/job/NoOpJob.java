package com.dreamscale.htmflow.core.gridtime.executor.machine.job;

import com.dreamscale.htmflow.core.gridtime.executor.clock.GeometryClock;
import com.dreamscale.htmflow.core.gridtime.executor.machine.instructions.TileInstructions;
import com.dreamscale.htmflow.core.gridtime.executor.memory.FeaturePool;

public class NoOpJob implements MetronomeJob {

    private final FeaturePool featurePool;

    public NoOpJob(FeaturePool featurePool) {
        this.featurePool = featurePool;
    }

    @Override
    public void gotoPosition(GeometryClock.GridTime coords) {
        featurePool.gotoPosition(coords);
    }

    @Override
    public TileInstructions baseTick(GeometryClock.GridTime fromGridTime, GeometryClock.GridTime toGridTime) {
        return null;
    }

    @Override
    public TileInstructions aggregateTick(GeometryClock.GridTime fromGridTime, GeometryClock.GridTime toGridTime) {
        return null;
    }
}
