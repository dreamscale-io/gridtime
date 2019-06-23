package com.dreamscale.htmflow.core.gridtime.executor.machine.parts.transform;

import com.dreamscale.htmflow.core.gridtime.executor.memory.FeaturePool;
import com.dreamscale.htmflow.core.gridtime.executor.memory.tile.GridTile;

public interface TransformStrategy {

    void transform(FeaturePool featurePool, GridTile gridTile);
}
