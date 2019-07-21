package com.dreamscale.htmflow.core.gridtime.machine.executor.instructions;

import com.dreamscale.htmflow.core.gridtime.machine.clock.GeometryClock;
import com.dreamscale.htmflow.core.gridtime.machine.clock.Metronome;
import com.dreamscale.htmflow.core.gridtime.machine.clock.ZoomLevel;
import com.dreamscale.htmflow.core.gridtime.machine.memory.FeaturePool;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GenerateAggregateUpTile extends TileInstructions {

    private final ZoomLevel zoomLevel;
    private final FeaturePool featurePool;
    private final Metronome.Tick tick;

    public GenerateAggregateUpTile(FeaturePool featurePool, Metronome.Tick tick) {
        this.featurePool = featurePool;
        this.zoomLevel = tick.getZoomLevel();
        this.tick = tick;
    }

    @Override
    protected void executeInstruction() throws InterruptedException {

    }

    @Override
    public String getCmdDescription() {
        return "generate aggregate tile for "+zoomLevel + " anchored at "+ tick.toDisplayString() ;
    }
}
