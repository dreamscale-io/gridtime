package com.dreamscale.htmflow.core.gridtime.executor.machine.parts.sink;

import com.dreamscale.htmflow.core.gridtime.executor.memory.tile.GridTile;

import java.util.UUID;

public interface SinkStrategy {

    void save(UUID torchieId, GridTile gridTile);

}
