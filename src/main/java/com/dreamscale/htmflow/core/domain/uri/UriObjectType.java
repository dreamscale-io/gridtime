package com.dreamscale.htmflow.core.domain.uri;

import lombok.*;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.UUID;

public enum UriObjectType {
    BOX,
    LOCATION,
    TRAVERSAL, CIRCLE_MESSAGE, PROJECT_CONTEXT, TASK_CONTEXT, INTENTION_CONTEXT, BRIDGE
}
