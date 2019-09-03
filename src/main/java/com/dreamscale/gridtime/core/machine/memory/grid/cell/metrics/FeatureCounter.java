package com.dreamscale.gridtime.core.machine.memory.grid.cell.metrics;

import com.dreamscale.gridtime.core.machine.commons.DefaultCollections;
import com.dreamscale.gridtime.core.machine.memory.feature.reference.FeatureReference;
import com.dreamscale.gridtime.core.machine.memory.feature.reference.PlaceReference;
import com.dreamscale.gridtime.core.machine.memory.type.FeatureType;
import com.dreamscale.gridtime.core.machine.memory.type.PlaceType;
import lombok.Getter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Getter
@ToString
public class FeatureCounter {

    private Map<FeatureType, FeatureSet> featureSetMap = DefaultCollections.map();

    public void countSample(FeatureReference feature) {
        if (feature != null) {
            FeatureSet featureSet = findOrCreateFeatureSet(feature.getFeatureType());
            featureSet.add(feature);
        }
    }

    private FeatureSet findOrCreateFeatureSet(FeatureType featureType) {
        FeatureSet featureSet = featureSetMap.get(featureType);
        if (featureSet == null) {
            featureSet = new FeatureSet(featureType);
            featureSetMap.put(featureType, featureSet);
        }
        return featureSet;
    }

    public int getDistinctFeatureCount(FeatureType featureType) {
        FeatureSet featureSet = findOrCreateFeatureSet(featureType);
        return featureSet.size();
    }

    public <T> List<T> getFeaturesForType(FeatureType featureType) {
        FeatureSet featureSet = findOrCreateFeatureSet(featureType);
        return (List<T>)featureSet.getFeatures();
    }


    private class FeatureSet {

        private FeatureType featureType;
        private Set<FeatureReference> distinctFeatureSet = DefaultCollections.set();

        public FeatureSet(FeatureType featureType) {
            this.featureType = featureType;
        }

        public int size() {
            return distinctFeatureSet.size();
        }

        public void add(FeatureReference feature) {
            distinctFeatureSet.add(feature);
        }

        public List<FeatureReference> getFeatures() {
            return new ArrayList<>(distinctFeatureSet);
        }
    }
}
