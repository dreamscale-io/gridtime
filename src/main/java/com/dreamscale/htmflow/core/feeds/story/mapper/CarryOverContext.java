package com.dreamscale.htmflow.core.feeds.story.mapper;

import com.dreamscale.htmflow.core.feeds.story.feature.FlowFeature;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A bag of properties that can be carried over from frame to frame
 */
public class CarryOverContext {

    private String contextOwner;

    private Map<String, FlowFeature> keyValueSerializablePairs = new LinkedHashMap<>();

    private Map<String, CarryOverContext> subContextMap = new LinkedHashMap<>();

    public CarryOverContext(String contextOwner) {
        this.contextOwner = contextOwner;
    }

    public void addKeyValue(String key, FlowFeature jsonSerializableFeature) {
        keyValueSerializablePairs.put(key, jsonSerializableFeature);
    }

    public void addKeyList(String key, List<? extends FlowFeature> jsonSerializableFeatureList) {
        keyValueSerializablePairs.put(key, new ListOfFlowFeatures(jsonSerializableFeatureList));
    }

    public FlowFeature getValue(String key) {
        return keyValueSerializablePairs.get(key);
    }

    public Set<String> keySet() {
        return keyValueSerializablePairs.keySet();
    }

    public CarryOverContext getSubContext(String subContextOwner) {
        return subContextMap.get(subContextOwner);
    }

    public void addSubContext(CarryOverContext carryOverContext) {
        subContextMap.put(carryOverContext.getContextOwner(), carryOverContext);
    }

    private String getContextOwner() {
        return contextOwner;
    }

    public List<? extends FlowFeature> getKeyList(String key) {
        ListOfFlowFeatures flowFeatures = (ListOfFlowFeatures) keyValueSerializablePairs.get(key);

        return flowFeatures.getOriginalTypedList();
    }

    private class ListOfFlowFeatures implements FlowFeature {
        private final List<? extends FlowFeature> featureList;

        ListOfFlowFeatures(List<? extends FlowFeature> featureList) {
            this.featureList = featureList;
        }

        public List<? extends FlowFeature> getOriginalTypedList() {
            return featureList;
        }
    }
}
