package com.dreamscale.htmflow.core.gridtime.machine.executor.program.parts.transform;

import com.dreamscale.htmflow.core.gridtime.machine.memory.FeaturePool;
import com.dreamscale.htmflow.core.gridtime.machine.memory.tile.GridTile;

public interface TransformStrategy {

    void transform(FeaturePool featurePool);
}
