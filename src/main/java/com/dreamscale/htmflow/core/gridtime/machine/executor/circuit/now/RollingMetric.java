package com.dreamscale.htmflow.core.gridtime.machine.executor.circuit.now;

import com.dreamscale.htmflow.core.gridtime.machine.clock.ZoomLevel;

public interface RollingMetric<T> {

    //TODO these are LRU caches of top locations

    void cascadeRollups(ZoomLevel zoomLevel, RollingMetric<T> metric );
}
