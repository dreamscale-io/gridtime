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

    private Map<String, FlowFeature> keyValueUriObjects = new LinkedHashMap<>();
    private Map<String, Details> keyValueDetailObjects = new LinkedHashMap<>();

    private Map<String, CarryOverContext> subContextMap = new LinkedHashMap<>();

    public CarryOverContext(String contextOwner) {
        this.contextOwner = contextOwner;
    }

    public void saveFeature(String key, FlowFeature jsonSerializableFeature) {
        keyValueUriObjects.put(key, jsonSerializableFeature);
    }

    public void saveDetails(String key, Details jsonSerializableFeature) {
        keyValueDetailObjects.put(key, jsonSerializableFeature);
    }

    public void saveFeatureList(String key, List<? extends FlowFeature> jsonSerializableFeatureList) {
        if (jsonSerializableFeatureList.size() > 0) {
            keyValueUriObjects.put(key, new ListOfFlowFeatures(jsonSerializableFeatureList));
        }
    }

    public FlowFeature getFeature(String key) {
        return keyValueUriObjects.get(key);
    }

    public Details getDetails(String key) {
        return keyValueDetailObjects.get(key);
    }

    public Set<String> featureKeySet() {
        return keyValueUriObjects.keySet();
    }
    public Set<String> detailsKeySet() {
        return keyValueDetailObjects.keySet();
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

    public List<? extends FlowFeature> getFeatureList(String key) {
        ListOfFlowFeatures flowFeatures = (ListOfFlowFeatures) keyValueUriObjects.get(key);

        if (flowFeatures != null) {
            return flowFeatures.getOriginalTypedList();
        } else {
            return new ArrayList<>();
        }

    }

}
