package com.dreamscale.htmflow.core.gridtime.machine.executor.program.parts.analytics;

import com.dreamscale.htmflow.core.gridtime.machine.executor.program.parts.source.Window;
import com.dreamscale.htmflow.core.gridtime.machine.memory.feed.Flowable;
import com.dreamscale.htmflow.core.gridtime.machine.memory.tile.GridTile;

public interface TileObserver<T extends Flowable> {

    void see(Window<T> window, GridTile gridTile);
}
