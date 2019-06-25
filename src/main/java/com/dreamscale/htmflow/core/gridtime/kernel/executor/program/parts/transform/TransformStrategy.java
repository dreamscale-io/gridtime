package com.dreamscale.htmflow.core.gridtime.kernel.executor.program.parts.transform;

import com.dreamscale.htmflow.core.gridtime.kernel.memory.FeaturePool;
import com.dreamscale.htmflow.core.gridtime.kernel.memory.tile.GridTile;

public interface TransformStrategy {

    void transform(FeaturePool featurePool, GridTile gridTile);
}
