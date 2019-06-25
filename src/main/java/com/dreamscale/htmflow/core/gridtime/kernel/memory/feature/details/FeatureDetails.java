package com.dreamscale.htmflow.core.gridtime.kernel.memory.feature.details;

import com.dreamscale.htmflow.core.gridtime.kernel.executor.program.parts.sink.JSONTransformer;

public interface FeatureDetails {

    String toSearchKey();

    default String toJson() {
        return JSONTransformer.toJson(this);
    }
}
