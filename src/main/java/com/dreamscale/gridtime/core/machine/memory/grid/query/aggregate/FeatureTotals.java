package com.dreamscale.gridtime.core.machine.memory.grid.query.aggregate;

import com.dreamscale.gridtime.core.machine.commons.DefaultCollections;
import com.dreamscale.gridtime.core.machine.memory.feature.reference.FeatureReference;
import com.dreamscale.gridtime.core.machine.memory.grid.cell.metrics.GridMetrics;

import java.util.Map;

public class FeatureTotals {

    private GridMetrics theVoid = new GridMetrics();
    private Map<FeatureReference, GridMetrics> metricsPerFeature = DefaultCollections.map();

    public GridMetrics getMetricsFor(FeatureReference featureReference) {
        if (featureReference == null) {
            return theVoid;
        }

        GridMetrics metrics = metricsPerFeature.get(featureReference);
        if (metrics == null) {
            metrics = new GridMetrics();
            metricsPerFeature.put(featureReference, metrics);
        }
        return metrics;
    }
}
