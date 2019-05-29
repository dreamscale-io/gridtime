package com.dreamscale.htmflow.core.gridtime.executor.machine.parts.transform;

import com.dreamscale.htmflow.core.gridtime.executor.memory.tile.GridTile;

public interface TransformStrategy {

    void transform(GridTile gridTile);
}
