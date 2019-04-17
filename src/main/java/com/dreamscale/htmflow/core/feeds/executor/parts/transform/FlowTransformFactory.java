package com.dreamscale.htmflow.core.feeds.executor.parts.transform;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class FlowTransformFactory {

    @Autowired
    private URIAssignmentTransform uriAssignmentTransformer;

    @Autowired
    private MusicPlayerTransform musicPlayerTransform;

    public FlowTransform get(TransformType transformType) {
        switch (transformType) {
            case URI_ASSIGNMENT_TRANSFORM:
                return uriAssignmentTransformer;
            case MUSIC_PLAYER_TRANSFORM:
                return musicPlayerTransform;

        }
        return null;
    }

    public enum TransformType {
        MUSIC_PLAYER_TRANSFORM,
        URI_ASSIGNMENT_TRANSFORM
    }
}
