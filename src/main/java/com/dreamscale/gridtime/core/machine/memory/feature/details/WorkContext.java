package com.dreamscale.gridtime.core.machine.memory.feature.details;

import com.dreamscale.gridtime.core.machine.memory.cache.SearchKeyGenerator;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class WorkContext implements FeatureDetails {

    private StructureLevel structureLevel;
    private UUID referenceId;
    private String description;

    @Override
    public String toSearchKey() {
        return SearchKeyGenerator.createContextSearchKey(structureLevel, referenceId);
    }

}
