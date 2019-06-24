package com.dreamscale.htmflow.core.gridtime.machine.executor.job.parts.observer;

import com.dreamscale.htmflow.core.gridtime.machine.executor.job.parts.source.Window;
import com.dreamscale.htmflow.core.gridtime.machine.memory.feed.Flowable;
import com.dreamscale.htmflow.core.gridtime.machine.memory.tile.GridTile;

public interface FlowObserver<T extends Flowable> {

    void see(Window<T> window, GridTile gridTile);
}
