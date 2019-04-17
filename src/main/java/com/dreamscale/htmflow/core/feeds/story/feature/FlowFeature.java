package com.dreamscale.htmflow.core.feeds.story.feature;

import com.dreamscale.htmflow.core.domain.uri.FlowObjectType;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public abstract class FlowFeature {

    private String uri;
    private String relativePath;
    private UUID id;
    private FlowObjectType flowObjectType;

    public FlowFeature() {
        id = UUID.randomUUID();
    }

}
