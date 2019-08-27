package com.dreamscale.gridtime.core.machine.executor.circuit.now;

import com.dreamscale.gridtime.core.machine.clock.ZoomLevel;

public interface RollingMetric<T> {

    //TODO these are LRU caches of top locations

    void cascadeRollups(ZoomLevel zoomLevel, RollingMetric<T> metric );
}
