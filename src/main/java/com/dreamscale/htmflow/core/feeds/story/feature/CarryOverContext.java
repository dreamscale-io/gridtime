package com.dreamscale.htmflow.core.feeds.story.feature;

import com.dreamscale.htmflow.core.feeds.story.feature.details.Details;
import lombok.*;

import java.util.*;

/**
 * A bag of properties that can be carried over from frame to frame
 */

@NoArgsConstructor
@Getter
@Setter
@ToString
public class CarryOverContext {

    private String contextOwner;

    private Map<String, FlowFeature> savedFeatureObjects = new LinkedHashMap<>();
    private Map<String, Details> savedDetailObjects = new LinkedHashMap<>();

    private Map<String, CarryOverContext> subContextMap = new LinkedHashMap<>();

    public CarryOverContext(String contextOwner) {
        this.contextOwner = contextOwner;
    }

    public void saveFeature(String key, FlowFeature jsonSerializableFeature) {
        if (jsonSerializableFeature != null) {
            savedFeatureObjects.put(key, jsonSerializableFeature);
        }
    }

    public void saveDetails(String key, Details jsonSerializableFeature) {
        if (jsonSerializableFeature != null) {
            savedDetailObjects.put(key, jsonSerializableFeature);
        }
    }

    public void saveFeatureList(String key, List<? extends FlowFeature> jsonSerializableFeatureList) {
        if (jsonSerializableFeatureList.size() > 0) {
            savedFeatureObjects.put(key, new ListOfFlowFeatures(jsonSerializableFeatureList));
        }
    }

    public FlowFeature getFeature(String key) {
        return savedFeatureObjects.get(key);
    }

    public Details getDetails(String key) {
        return savedDetailObjects.get(key);
    }

    public Set<String> featureKeySet() {
        return savedFeatureObjects.keySet();
    }
    public Set<String> detailsKeySet() {
        return savedDetailObjects.keySet();
    }

    public CarryOverContext getSubContext(String subContextOwner) {
        CarryOverContext subContext = subContextMap.get(subContextOwner);
        if (subContext == null) {
            subContext = new CarryOverContext(subContextOwner);
        }
        return subContext;
    }

    public void addSubContext(CarryOverContext carryOverContext) {
        if (carryOverContext.isNotEmpty()) {
            subContextMap.put(carryOverContext.getContextOwner(), carryOverContext);
        }
    }

    private boolean isNotEmpty() {
        return savedDetailObjects.size() > 0 || savedFeatureObjects.size() > 0;
    }


    public List<? extends FlowFeature> getFeatureList(String key) {
        ListOfFlowFeatures flowFeatures = (ListOfFlowFeatures) savedFeatureObjects.get(key);

        if (flowFeatures != null) {
            return flowFeatures.getOriginalTypedList();
        } else {
            return new ArrayList<>();
        }

    }

}
