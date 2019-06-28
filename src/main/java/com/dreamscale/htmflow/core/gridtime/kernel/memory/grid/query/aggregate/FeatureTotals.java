package com.dreamscale.htmflow.core.gridtime.kernel.memory.grid.query.aggregate;

import com.dreamscale.htmflow.core.gridtime.kernel.commons.DefaultCollections;
import com.dreamscale.htmflow.core.gridtime.kernel.memory.cache.FeatureCache;
import com.dreamscale.htmflow.core.gridtime.kernel.memory.feature.reference.FeatureReference;
import com.dreamscale.htmflow.core.gridtime.kernel.memory.grid.cell.metrics.GridMetrics;

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
