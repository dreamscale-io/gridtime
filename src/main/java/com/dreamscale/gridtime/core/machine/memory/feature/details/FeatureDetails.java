package com.dreamscale.gridtime.core.machine.memory.feature.details;

import com.dreamscale.gridtime.core.machine.commons.JSONTransformer;

public interface FeatureDetails {

    String toSearchKey();

    default String toJson() {
        return JSONTransformer.toJson(this);
    }
}
