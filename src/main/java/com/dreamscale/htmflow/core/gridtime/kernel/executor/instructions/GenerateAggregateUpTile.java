package com.dreamscale.htmflow.core.gridtime.kernel.executor.instructions;

import com.dreamscale.htmflow.core.gridtime.kernel.clock.GeometryClock;
import com.dreamscale.htmflow.core.gridtime.kernel.clock.ZoomLevel;
import com.dreamscale.htmflow.core.gridtime.kernel.memory.FeaturePool;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GenerateAggregateUpTile extends TileInstructions {

    private final ZoomLevel zoomLevel;
    private final FeaturePool featurePool;
    private final GeometryClock.GridTime fromClockPosition;
    private final GeometryClock.GridTime toClockPosition;

    public GenerateAggregateUpTile(FeaturePool featurePool, GeometryClock.GridTime fromGridTime, GeometryClock.GridTime toGridTime) {
        this.featurePool = featurePool;
        this.zoomLevel = fromGridTime.getZoomLevel();
        this.fromClockPosition = fromGridTime;
        this.toClockPosition = toGridTime;
    }

    @Override
    protected void executeInstruction() throws InterruptedException {

    }

    @Override
    public String getCmdDescription() {
        return "generate aggregate tile for "+zoomLevel + " anchored at "+ fromClockPosition ;
    }
}
