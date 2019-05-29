package com.dreamscale.htmflow.core.gridtime.executor.memory.feature.details;

import com.dreamscale.htmflow.core.gridtime.executor.machine.parts.sink.JSONTransformer;

public interface FeatureDetails {

    String toSearchKey();

    default String toJson() {
        return JSONTransformer.toJson(this);
    }
}
