package com.dreamscale.htmflow.core.gridtime.kernel.executor.instructions;

import com.dreamscale.htmflow.core.gridtime.kernel.clock.Metronome;
import com.dreamscale.htmflow.core.gridtime.kernel.memory.FeaturePool;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ResumeMetronome extends TileInstructions {

    private final FeaturePool featurePool;
    private final Metronome metronome;

    public ResumeMetronome(FeaturePool featurePool, Metronome metronome) {
        this.featurePool = featurePool;
        this.metronome = metronome;
    }

    @Override
    protected void executeInstruction() throws InterruptedException {
        this.metronome.resume();
    }

    @Override
    public String getCmdDescription() {
        return "resuming metronome";
    }
}
