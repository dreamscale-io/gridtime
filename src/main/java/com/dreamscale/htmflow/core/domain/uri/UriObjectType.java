package com.dreamscale.htmflow.core.domain.uri;

import lombok.*;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.UUID;

public enum UriObjectType {
    BOX,
    LOCATION,
    TRAVERSAL, BRIDGE
}
