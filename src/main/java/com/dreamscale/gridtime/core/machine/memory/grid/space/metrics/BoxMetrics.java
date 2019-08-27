package com.dreamscale.gridtime.core.machine.memory.grid.space.metrics;

import com.dreamscale.gridtime.core.machine.commons.DefaultCollections;
import com.dreamscale.gridtime.core.machine.memory.feature.reference.ExecutionReference;
import com.dreamscale.gridtime.core.machine.memory.feature.reference.PlaceReference;
import com.dreamscale.gridtime.core.machine.memory.grid.cell.metrics.CandleStick;
import com.dreamscale.gridtime.core.machine.memory.grid.cell.metrics.GridMetrics;

import java.time.Duration;
import java.util.Map;

public class BoxMetrics {

    private PlaceReference box;

    private GridMetrics boxMetrics;

    private Map<PlaceReference, LocationMetrics> locationMetricsMap = DefaultCollections.map();
    private Map<PlaceReference, TraversalMetrics> traversalMetricsMap = DefaultCollections.map();

    private Duration timeInBox;
    private Duration timeInWtf;
    private Duration timeInLearning;
    private Duration timeInProgress;
    private Duration timeInPairing;

    private CandleStick flame;
    private CandleStick batchSize;
    private CandleStick traversalSpeed;
    private CandleStick executionTime;
    private CandleStick redToGreenTime;

    public BoxMetrics(PlaceReference boxReference) {
        this.box = boxReference;
        this.boxMetrics = new GridMetrics();
    }

    public void visitLocation(PlaceReference locationReference, Duration timeInLocation) {
        LocationMetrics locationMetrics = findOrCreateLocationMetrics(locationReference);
        locationMetrics.visit(timeInLocation);
    }

    public void visitTraversal(PlaceReference traversalReference) {
        TraversalMetrics traversalMetrics = findOrCreateTraversalMetrics(traversalReference);
        traversalMetrics.visit();
    }

    private LocationMetrics findOrCreateLocationMetrics(PlaceReference locationReference ) {
        LocationMetrics locationMetrics = locationMetricsMap.get(locationReference);
        if (locationMetrics == null) {
            locationMetrics = new LocationMetrics(locationReference, boxMetrics);
            locationMetricsMap.put(locationReference, locationMetrics);
        }
        return locationMetrics;
    }

    private TraversalMetrics findOrCreateTraversalMetrics(PlaceReference traversalReference ) {
        TraversalMetrics traversalMetrics = traversalMetricsMap.get(traversalReference);
        if (traversalMetrics == null) {
            traversalMetrics = new TraversalMetrics(traversalReference);
            traversalMetricsMap.put(traversalReference, traversalMetrics);
        }
        return traversalMetrics;
    }

    public void modify(int modificationCount) {
        boxMetrics.addModificationSample(modificationCount);
    }

    public void execute(ExecutionReference executionReference) {
        boxMetrics.addExecutionTimeSample(executionReference.getExecutionTime());
    }
}
