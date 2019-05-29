package com.dreamscale.htmflow.core.gridtime.executor.machine.parts.transform;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class FlowTransformFactory {

    @Autowired
    private URIAssignmentTransform uriAssignmentTransformer;

    public TransformStrategy get(TransformType transformType) {
        switch (transformType) {
            case URI_ASSIGNMENT_TRANSFORM:
                return uriAssignmentTransformer;
        }
        return null;
    }

    public enum TransformType {
        URI_ASSIGNMENT_TRANSFORM
    }
}
