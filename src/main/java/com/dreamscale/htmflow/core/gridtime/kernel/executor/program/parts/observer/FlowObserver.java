package com.dreamscale.htmflow.core.gridtime.kernel.executor.program.parts.observer;

import com.dreamscale.htmflow.core.gridtime.kernel.executor.program.parts.source.Window;
import com.dreamscale.htmflow.core.gridtime.kernel.memory.feed.Flowable;
import com.dreamscale.htmflow.core.gridtime.kernel.memory.tile.GridTile;

public interface FlowObserver<T extends Flowable> {

    void see(Window<T> window, GridTile gridTile);
}
