package com.dreamscale.htmflow.core.gridtime.executor.machine.instructions;

import com.dreamscale.htmflow.core.gridtime.executor.clock.Metronome;
import com.dreamscale.htmflow.core.gridtime.executor.memory.FeaturePool;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class HaltMetronome extends TileInstructions {

    private final FeaturePool featurePool;
    private final Metronome metronome;

    public HaltMetronome(FeaturePool featurePool, Metronome metronome) {
        this.featurePool = featurePool;
        this.metronome = metronome;
    }

    @Override
    protected void executeInstruction() throws InterruptedException {
        this.metronome.halt();
    }

    @Override
    public String getCmdDescription() {
        return "halting metronome";
    }
}
