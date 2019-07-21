package com.dreamscale.htmflow.core.gridtime.machine.executor.instructions;

import com.dreamscale.htmflow.core.gridtime.machine.clock.GeometryClock;
import com.dreamscale.htmflow.core.gridtime.machine.clock.Metronome;
import com.dreamscale.htmflow.core.gridtime.machine.executor.circuit.Flow;
import com.dreamscale.htmflow.core.gridtime.machine.memory.FeaturePool;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class GenerateBaseTile extends TileInstructions {

    private final List<Flow> pullChain;
    private final FeaturePool featurePool;

    private final Metronome.Tick tick;


    public GenerateBaseTile(FeaturePool featurePool, List<Flow> pullChain, Metronome.Tick tick) {
        this.featurePool = featurePool;
        this.pullChain = pullChain;
        this.tick = tick;
    }

    @Override
    public void executeInstruction() throws InterruptedException {
        featurePool.nextGridTile();

        for (Flow flow : pullChain) {
            flow.tick(tick);
        }

        setOutputTile(featurePool.getActiveGridTile());
    }

    @Override
    public String getCmdDescription() {
        return "generate tile for "+tick.toDisplayString();
    }
}
