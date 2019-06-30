package com.dreamscale.htmflow.core.gridtime.machine.memory.grid.space.metrics;

import com.dreamscale.htmflow.core.gridtime.machine.memory.feature.reference.PlaceReference;

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
