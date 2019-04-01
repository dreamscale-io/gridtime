package com.dreamscale.htmflow.core.feeds.story;

import com.dreamscale.htmflow.core.feeds.story.feature.FlowFeature;
import com.dreamscale.htmflow.core.feeds.story.mapper.FlowContextMapper;

import java.util.LinkedHashMap;
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
}
