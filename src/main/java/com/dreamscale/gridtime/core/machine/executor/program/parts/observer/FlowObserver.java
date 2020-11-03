package com.dreamscale.gridtime.core.machine.executor.program.parts.observer;

import com.dreamscale.gridtime.core.machine.executor.program.parts.source.Window;
import com.dreamscale.gridtime.core.machine.memory.feed.Flowable;
import com.dreamscale.gridtime.core.machine.memory.tile.GridTile;

public interface FlowObserver<T extends Flowable> {

    void observe(Window<T> window, GridTile gridTile);
}
