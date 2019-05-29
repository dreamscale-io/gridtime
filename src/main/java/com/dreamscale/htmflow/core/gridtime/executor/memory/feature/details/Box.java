package com.dreamscale.htmflow.core.gridtime.executor.memory.feature.details;

import com.dreamscale.htmflow.core.gridtime.executor.memory.search.SearchKeyGenerator;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.UUID;

@Getter
@Setter
@ToString
public class Box implements FeatureDetails {

    private final UUID projectId;
    private String boxName;

    public Box(UUID projectId, String boxName) {
        this.projectId = projectId;
        this.boxName = boxName;
    }

    @Override
    public String toSearchKey() {
        return SearchKeyGenerator.createBoxKey(boxName);
    }
}
