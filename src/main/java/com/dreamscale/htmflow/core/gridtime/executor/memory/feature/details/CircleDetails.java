package com.dreamscale.htmflow.core.gridtime.executor.memory.feature.details;

import com.dreamscale.htmflow.core.gridtime.executor.memory.search.SearchKeyGenerator;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.UUID;

@Getter
@Setter
@ToString
@AllArgsConstructor
public class CircleDetails implements FeatureDetails {

    private UUID circleId;
    private String circleName;

    public String toSearchKey() {
        return SearchKeyGenerator.createCircleSearchKey(circleId);
    }

    public String toString() {
        return toSearchKey();
    }
}
