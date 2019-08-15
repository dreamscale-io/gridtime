package com.dreamscale.htmflow.core.gridtime.machine.memory.feature.details;

import com.dreamscale.htmflow.core.gridtime.machine.commons.JSONTransformer;

public interface FeatureDetails {

    String toSearchKey();

    default String toJson() {
        return JSONTransformer.toJson(this);
    }
}
