package com.dreamscale.htmflow.core.gridtime.kernel.executor.program;

import com.dreamscale.htmflow.core.gridtime.kernel.clock.GeometryClock;
import com.dreamscale.htmflow.core.gridtime.kernel.executor.instructions.TileInstructions;
import com.dreamscale.htmflow.core.gridtime.kernel.memory.FeaturePool;

import java.time.LocalDateTime;

public class NoOpProgram implements MetronomeProgram {

    private final FeaturePool featurePool;

    public NoOpProgram(FeaturePool featurePool) {
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
