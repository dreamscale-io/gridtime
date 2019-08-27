package com.dreamscale.gridtime.core.machine.executor.program.parts.analytics;

import com.dreamscale.gridtime.core.machine.executor.program.parts.source.Window;
import com.dreamscale.gridtime.core.machine.memory.feed.Flowable;
import com.dreamscale.gridtime.core.machine.memory.tile.GridTile;

public interface TileObserver<T extends Flowable> {

    void see(Window<T> window, GridTile gridTile);
}
