package com.dreamscale.htmflow.core.gridtime.kernel.executor.circuit.now;

import com.dreamscale.htmflow.core.gridtime.kernel.clock.ZoomLevel;

public interface RollingMetric<T> {

    //TODO these are LRU caches of top locations

    void cascadeRollups(ZoomLevel zoomLevel, RollingMetric<T> metric );
}
