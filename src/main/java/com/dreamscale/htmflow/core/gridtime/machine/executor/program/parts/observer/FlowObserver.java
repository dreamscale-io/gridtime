package com.dreamscale.htmflow.core.gridtime.machine.executor.program.parts.observer;

import com.dreamscale.htmflow.core.gridtime.machine.executor.program.parts.source.Window;
import com.dreamscale.htmflow.core.gridtime.machine.memory.TorchieState;
import com.dreamscale.htmflow.core.gridtime.machine.memory.feed.Flowable;

public interface FlowObserver<T extends Flowable> {

    void see(Window<T> window, TorchieState torchieState);
}
