package com.dreamscale.gridtime.core.machine.memory.feature.details;

import com.dreamscale.gridtime.core.machine.memory.cache.SearchKeyGenerator;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.UUID;

@Getter
@Setter
@ToString
@AllArgsConstructor
public class WorkContext implements FeatureDetails {

    private StructureLevel structureLevel;
    private UUID referenceId;
    private String description;

    @Override
    public String toSearchKey() {
        return SearchKeyGenerator.createContextSearchKey(structureLevel, referenceId);
    }

}
