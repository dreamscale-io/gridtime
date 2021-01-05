package com.dreamscale.gridtime.core.machine.memory.tile;

import com.dreamscale.gridtime.core.machine.commons.DefaultCollections;
import com.dreamscale.gridtime.core.machine.memory.feature.reference.FeatureReference;
import com.dreamscale.gridtime.core.machine.memory.grid.cell.metrics.RollingAggregate;
import com.dreamscale.gridtime.core.machine.memory.tag.Tag;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@NoArgsConstructor
@Getter
@Setter
@ToString
public class ExportedCarryOverContext {

    private String contextOwner;

    private Map<String, UUID> savedFeatureIds = DefaultCollections.map();

    private Map<String, Tag> savedTags = DefaultCollections.map();

    private Map<String, Boolean> savedStateFlags = DefaultCollections.map();

    private Map<String, RollingAggregate> savedRollingAggregates = DefaultCollections.map();

    private Map<String, ExportedCarryOverContext> subContextMap = DefaultCollections.map();

    public ExportedCarryOverContext(String contextOwner) {
        this.contextOwner = contextOwner;
    }

    @JsonIgnore
    public Set<UUID> getAllFeatureIds() {
        Set<UUID> featureSet = new HashSet<>();

        featureSet.addAll(savedFeatureIds.values());

        for (ExportedCarryOverContext subcontext : subContextMap.values()) {
            featureSet.addAll(subcontext.getSavedFeatureIds().values());
        }

        return featureSet;
    }

}
