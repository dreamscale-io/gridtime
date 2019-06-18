package com.dreamscale.htmflow.core.gridtime.executor.machine.parts.observer;

import com.dreamscale.htmflow.core.gridtime.executor.machine.parts.source.Window;
import com.dreamscale.htmflow.core.gridtime.executor.memory.feed.Flowable;
import com.dreamscale.htmflow.core.gridtime.executor.memory.tile.GridTile;

import java.util.List;

public interface FlowObserver<T extends Flowable> {

    void see(Window<T> window, GridTile gridTile);
}
