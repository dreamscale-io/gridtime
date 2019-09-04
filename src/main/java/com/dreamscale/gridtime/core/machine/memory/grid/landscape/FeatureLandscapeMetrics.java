package com.dreamscale.gridtime.core.machine.memory.grid.landscape;

import com.dreamscale.gridtime.core.machine.commons.DefaultCollections;
import com.dreamscale.gridtime.core.machine.memory.feature.reference.FeatureReference;
import com.dreamscale.gridtime.core.machine.memory.feature.reference.PlaceReference;
import com.dreamscale.gridtime.core.machine.memory.grid.cell.metrics.FeatureCounter;
import com.dreamscale.gridtime.core.machine.memory.grid.cell.metrics.GridMetrics;
import com.dreamscale.gridtime.core.machine.memory.type.FeatureType;
import com.dreamscale.gridtime.core.machine.memory.type.PlaceType;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class FeatureLandscapeMetrics {

    private GridMetrics theVoid = new GridMetrics();

    private Map<FeatureType, LandscapeLayer> landscapeLayers = DefaultCollections.map();

    private FeatureCounter featureCounter = new FeatureCounter();

    public List<PlaceReference> getBoxesVisited() {
        return featureCounter.getFeaturesForType(PlaceType.BOX);
    }

    public GridMetrics getMetricsFor(FeatureReference feature) {
        if (feature == null) {
            return theVoid;
        } else {
            LandscapeLayer landscapeLayer = findOrCreateLandscapeLayer(feature.getFeatureType());
            featureCounter.countSample(feature);

            return landscapeLayer.getMetricsFor(feature);
        }
    }

    private LandscapeLayer findOrCreateLandscapeLayer(FeatureType featureType) {
        LandscapeLayer layer = landscapeLayers.get(featureType);
        if (layer == null) {
            layer = new LandscapeLayer(featureType);
            landscapeLayers.put(featureType, layer);
        }

        return layer;
    }

    public Set<FeatureReference> getFeaturesOfType(FeatureType featureType) {
        LandscapeLayer landscapeLayer = findOrCreateLandscapeLayer(featureType);

        return landscapeLayer.getFeatures();
    }


    private class LandscapeLayer {
        private final FeatureType featureType;
        private final Map<FeatureReference, GridMetrics> metricsPerFeature = DefaultCollections.map();

        public LandscapeLayer(FeatureType featureType) {
            this.featureType = featureType;
        }

        public FeatureType getFeatureType() {
            return featureType;
        }

        public Set<FeatureReference> getFeatures() {
            return metricsPerFeature.keySet();
        }

        public GridMetrics getMetricsFor(FeatureReference featureReference) {
            if (featureReference == null) {
                return null;
            }

            GridMetrics metrics = metricsPerFeature.get(featureReference);
            if (metrics == null) {
                metrics = new GridMetrics();
                metricsPerFeature.put(featureReference, metrics);
            }
            return metrics;
        }

    }
}
