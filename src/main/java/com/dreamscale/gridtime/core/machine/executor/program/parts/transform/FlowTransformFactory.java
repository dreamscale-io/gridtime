package com.dreamscale.gridtime.core.machine.executor.program.parts.transform;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class FlowTransformFactory {

    private ResolveFeaturesTransform resolveFeaturesTransform = new ResolveFeaturesTransform();

    @Autowired
    private DeleteOldTileTransform deleteOldTileTransform;

    public TransformStrategy get(TransformType transformType) {
        switch (transformType) {
            case RESOLVE_FEATURES_TRANSFORM:
                return resolveFeaturesTransform;
            case DELETE_OLD_TILE_TRANSFORM:
                return deleteOldTileTransform;
        }
        return null;
    }

    public enum TransformType {
        RESOLVE_FEATURES_TRANSFORM,
        DELETE_OLD_TILE_TRANSFORM
    }
}
