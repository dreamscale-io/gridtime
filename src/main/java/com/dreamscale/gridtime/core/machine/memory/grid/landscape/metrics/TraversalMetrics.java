package com.dreamscale.gridtime.core.machine.memory.grid.landscape.metrics;

import com.dreamscale.gridtime.core.machine.memory.feature.reference.PlaceReference;

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
