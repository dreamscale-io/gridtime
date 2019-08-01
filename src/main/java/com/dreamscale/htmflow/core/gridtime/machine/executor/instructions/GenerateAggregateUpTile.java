package com.dreamscale.htmflow.core.gridtime.machine.executor.instructions;

import com.dreamscale.htmflow.core.gridtime.machine.clock.Metronome;
import com.dreamscale.htmflow.core.gridtime.machine.clock.ZoomLevel;
import com.dreamscale.htmflow.core.gridtime.machine.memory.TorchieState;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GenerateAggregateUpTile extends TileInstructions {

    private final ZoomLevel zoomLevel;
    private final TorchieState torchieState;
    private final Metronome.Tick tick;

    public GenerateAggregateUpTile(TorchieState torchieState, Metronome.Tick tick) {
        this.torchieState = torchieState;
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
