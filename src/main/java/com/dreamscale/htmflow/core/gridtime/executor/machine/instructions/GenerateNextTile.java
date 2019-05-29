package com.dreamscale.htmflow.core.gridtime.executor.machine.instructions;

import com.dreamscale.htmflow.core.gridtime.executor.clock.GeometryClock;
import com.dreamscale.htmflow.core.gridtime.executor.machine.parts.commons.Flow;
import com.dreamscale.htmflow.core.gridtime.executor.memory.FeaturePool;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class GenerateNextTile extends TileInstructions {

    private final List<Flow> pullChain;
    private final FeaturePool featurePool;

    private final GeometryClock.Coords fromPosition;
    private final GeometryClock.Coords toPosition;


    public GenerateNextTile(FeaturePool featurePool, List<Flow> pullChain, GeometryClock.Coords fromPosition,  GeometryClock.Coords toPosition ) {
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
