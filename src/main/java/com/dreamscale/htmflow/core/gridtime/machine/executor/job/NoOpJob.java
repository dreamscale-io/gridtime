package com.dreamscale.htmflow.core.gridtime.machine.executor.job;

import com.dreamscale.htmflow.core.gridtime.machine.clock.GeometryClock;
import com.dreamscale.htmflow.core.gridtime.machine.executor.instructions.TileInstructions;
import com.dreamscale.htmflow.core.gridtime.machine.memory.FeaturePool;

import java.time.LocalDateTime;

public class NoOpJob implements MetronomeJob {

    private final FeaturePool featurePool;

    public NoOpJob(FeaturePool featurePool) {
        this.featurePool = featurePool;
    }

    @Override
    public LocalDateTime getStartPosition() {
        return LocalDateTime.now();
    }

    @Override
    public boolean canTick(LocalDateTime nextPosition) {
        return false;
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
