package com.dreamscale.gridtime.core.machine.memory.tile;

import com.dreamscale.gridtime.core.machine.memory.feature.reference.FeatureReference;
import com.dreamscale.gridtime.core.machine.commons.DefaultCollections;
import com.dreamscale.gridtime.core.machine.memory.tag.Tag;
import com.dreamscale.gridtime.core.machine.memory.grid.cell.metrics.RollingAggregate;
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

    private Map<String, FeatureReference> savedFeatureReferences = DefaultCollections.map();
    private Map<String, Tag> savedTags = DefaultCollections.map();
    private Map<String, Boolean> savedStateFlags = DefaultCollections.map();

    private Map<String, RollingAggregate> savedRollingAggregates = DefaultCollections.map();


    private Map<String, CarryOverContext> subContextMap = DefaultCollections.map();

    public CarryOverContext(String contextOwner) {
        this.contextOwner = contextOwner;
    }

    public CarryOverContext getSubContext(String subContextOwner) {
        CarryOverContext subContext = subContextMap.get(subContextOwner);
        if (subContext == null) {
            subContext = new CarryOverContext(subContextOwner);
        }
        return subContext;
    }

    public void addSubContext(CarryOverContext carryOverContext) {
        if (carryOverContext != null && carryOverContext.isNotEmpty()) {
            subContextMap.put(carryOverContext.getContextOwner(), carryOverContext);
        }
    }

    private boolean isNotEmpty() {
        return savedFeatureReferences.size() > 0 || savedTags.size() > 0 || savedStateFlags.size() > 0;
    }

    public void saveReference(String key, FeatureReference reference) {
        if (reference != null) {
            savedFeatureReferences.put(key, reference);
        }
    }

    public void saveRollingAggregate(String key, RollingAggregate aggregate) {
        savedRollingAggregates.put(key, aggregate);
    }

    public RollingAggregate getRollingAggregate(String key) {
        return savedRollingAggregates.get(key);
    }

    public <F extends FeatureReference> F getReference(String key) {
        return (F)savedFeatureReferences.get(key);
    }

    public Set<String> getReferenceKeys() {
        return savedFeatureReferences.keySet();
    }

    public void saveTag(String key, Tag tag) {
        savedTags.put(key, tag);
    }

    public void saveStateFlag(String key, boolean flag) {
        savedStateFlags.put(key, flag);
    }

    public <T> T getTag(String key) {
        return (T) savedTags.get(key);
    }

    public Boolean getStateFlag(String key) {
        return savedStateFlags.get(key);
    }

}
