package com.dreamscale.htmflow.core.gridtime.executor.machine.instructions;

import com.dreamscale.htmflow.core.gridtime.executor.clock.GeometryClock;
import com.dreamscale.htmflow.core.gridtime.executor.clock.ZoomLevel;
import com.dreamscale.htmflow.core.gridtime.executor.memory.FeaturePool;
import com.dreamscale.htmflow.core.gridtime.executor.memory.tile.GridTile;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

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
