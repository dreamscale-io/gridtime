package com.dreamscale.htmflow.core.gridtime.executor.machine.instructions;

import com.dreamscale.htmflow.core.gridtime.executor.clock.ZoomLevel;
import com.dreamscale.htmflow.core.gridtime.executor.memory.FeaturePool;
import com.dreamscale.htmflow.core.gridtime.executor.memory.tile.GridTile;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

@Slf4j
public class GenerateAggregateUpTile extends TileInstructions {

    private final ZoomLevel zoomLevel;
    private final LocalDateTime fromClockPosition;
    private final FeaturePool featurePool;

    public GenerateAggregateUpTile(FeaturePool featurePool, ZoomLevel zoomLevel, LocalDateTime fromClockPosition) {
        this.featurePool = featurePool;
        this.zoomLevel = zoomLevel;
        this.fromClockPosition = fromClockPosition;
    }

    @Override
    protected void executeInstruction() throws InterruptedException {

    }

    @Override
    public String getCmdDescription() {
        return "generate aggregate tile for "+zoomLevel + " anchored at "+ fromClockPosition ;
    }
}
