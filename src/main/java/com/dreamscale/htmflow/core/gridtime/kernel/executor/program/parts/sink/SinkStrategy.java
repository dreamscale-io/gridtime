package com.dreamscale.htmflow.core.gridtime.kernel.executor.program.parts.sink;

import com.dreamscale.htmflow.core.gridtime.kernel.memory.tile.GridTile;

import java.util.UUID;

public interface SinkStrategy {

    void save(UUID torchieId, GridTile gridTile);

}
