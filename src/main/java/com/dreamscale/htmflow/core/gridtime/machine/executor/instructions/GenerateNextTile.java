package com.dreamscale.htmflow.core.gridtime.machine.executor.instructions;

import com.dreamscale.htmflow.core.gridtime.machine.clock.GeometryClock;
import com.dreamscale.htmflow.core.gridtime.machine.executor.circuit.Flow;
import com.dreamscale.htmflow.core.gridtime.machine.memory.FeaturePool;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class GenerateNextTile extends TileInstructions {

    private final List<Flow> pullChain;
    private final FeaturePool featurePool;

    private final GeometryClock.GridTime fromPosition;
    private final GeometryClock.GridTime toPosition;


    public GenerateNextTile(FeaturePool featurePool, List<Flow> pullChain, GeometryClock.GridTime fromPosition, GeometryClock.GridTime toPosition ) {
        this.featurePool = featurePool;
        this.pullChain = pullChain;
        this.fromPosition = fromPosition;
        this.toPosition = toPosition;
    }

    @Override
    public void executeInstruction() throws InterruptedException {
        featurePool.nextGridTile(fromPosition);

        for (Flow flow : pullChain) {
            flow.tick(fromPosition.getClockTime(), toPosition.getClockTime());
        }

        setOutputTile(featurePool.getActiveGridTile());
    }

    @Override
    public String getCmdDescription() {
        return "generate tile for "+fromPosition.getFormattedGridTime();
    }
}
