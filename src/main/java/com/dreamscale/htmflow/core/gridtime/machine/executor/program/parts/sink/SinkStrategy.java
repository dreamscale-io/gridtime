package com.dreamscale.htmflow.core.gridtime.machine.executor.program.parts.sink;

import com.dreamscale.htmflow.core.gridtime.machine.memory.FeaturePool;
import com.dreamscale.htmflow.core.gridtime.machine.memory.tile.GridTile;

import java.util.UUID;

public interface SinkStrategy {

    void save(UUID torchieId, FeaturePool featurePool);

}
