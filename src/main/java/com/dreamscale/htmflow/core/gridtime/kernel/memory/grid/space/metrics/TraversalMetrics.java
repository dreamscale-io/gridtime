package com.dreamscale.htmflow.core.gridtime.kernel.memory.grid.space.metrics;

import com.dreamscale.htmflow.core.gridtime.kernel.memory.feature.reference.PlaceReference;
import com.dreamscale.htmflow.core.gridtime.kernel.memory.grid.cell.metrics.CandleStick;

import java.time.Duration;

public class TraversalMetrics {

    private PlaceReference traversal;

    private int visitCount;

    public TraversalMetrics(PlaceReference traversalReference) {
        this.traversal = traversalReference;
    }

    public void visit() {
        visitCount++;
    }
}
