package com.dreamscale.gridtime.core.machine.memory.feature.details;

import com.dreamscale.gridtime.core.machine.memory.cache.SearchKeyGenerator;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
public class CircuitDetails implements FeatureDetails {

    private UUID circleId;
    private String circleName;

    public String toSearchKey() {
        return SearchKeyGenerator.createCircleSearchKey(circleId);
    }

    public String toString() {
        return toSearchKey();
    }
}
