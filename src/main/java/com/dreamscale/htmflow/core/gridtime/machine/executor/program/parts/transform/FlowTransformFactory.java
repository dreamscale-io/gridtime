package com.dreamscale.htmflow.core.gridtime.machine.executor.program.parts.transform;

import org.springframework.stereotype.Component;

@Component
public class FlowTransformFactory {

    private ResolveFeaturesTransform resolveFeaturesTransform = new ResolveFeaturesTransform();

    public TransformStrategy get(TransformType transformType) {
        switch (transformType) {
            case RESOLVE_FEATURES_TRANSFORM:
                return resolveFeaturesTransform;
        }
        return null;
    }

    public enum TransformType {
        RESOLVE_FEATURES_TRANSFORM
    }
}
